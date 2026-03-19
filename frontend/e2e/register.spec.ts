import { expect, Page, test } from '@playwright/test';

type InvitationOptions = {
  token?: string;
  email?: string;
  projectName?: string;
  role?: 'ADMIN' | 'MEMBER' | 'OBSERVER';
  status?: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED' | 'CANCELED';
};

async function mockApi(
  page: Page,
  options: {
    invitation?: InvitationOptions;
  } = {},
): Promise<void> {
  const invitation = options.invitation ?? null;

  await page.route('**/api/**', async (route, request) => {
    const url = new URL(request.url());
    const { pathname } = url;

    if (pathname === '/api/users' && request.method() === 'POST') {
      const payload = request.postDataJSON() as { username?: string; email?: string } | null;

      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 99,
          username: payload?.username ?? 'alice',
          email: payload?.email ?? 'alice@example.com',
        }),
      });
      return;
    }

    if (pathname === '/api/users') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 99,
            username: 'alice',
            email: invitation?.email ?? 'alice@example.com',
          },
        ]),
      });
      return;
    }

    if (pathname === '/api/projects') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 1,
            name: invitation?.projectName ?? 'Projet E2E',
            description: 'Projet mocke',
            owner: { id: 99 },
          },
        ]),
      });
      return;
    }

    if (pathname === '/api/tasks') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 10,
            title: 'Verifier inscription',
            description: 'Controle e2e',
            status: 'TODO',
            priority: 'MEDIUM',
            dueDate: '2026-03-20',
            endDate: '2026-03-20',
            project: { id: 1 },
            createdBy: { id: 99 },
            assignedTo: { id: 99 },
          },
        ]),
      });
      return;
    }

    if (pathname === '/api/project-members') {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
          {
            id: 201,
            role: 'ADMIN',
            project: { id: 1 },
            user: { id: 99 },
          },
        ]),
      });
      return;
    }

    if (invitation && pathname === `/api/project-invitations/token/${invitation.token ?? 'invite-123'}`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          id: 301,
          email: invitation.email ?? 'invitee@example.com',
          role: invitation.role ?? 'MEMBER',
          status: invitation.status ?? 'PENDING',
          projectId: 1,
          projectName: invitation.projectName ?? 'Projet Invitation',
          expiresAt: '2026-03-25T12:00:00.000Z',
        }),
      });
      return;
    }

    if (invitation && pathname === `/api/project-invitations/token/${invitation.token ?? 'invite-123'}/accept`) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ projectId: 1 }),
      });
      return;
    }

    if (
      pathname === '/api/project-invitations'
      || pathname === '/api/notifications'
      || pathname === '/api/task-histories'
    ) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([]),
      });
      return;
    }

    await route.fulfill({
      status: 404,
      contentType: 'application/json',
      body: JSON.stringify({ message: `Unhandled route in test: ${pathname}` }),
    });
  });
}

test('shows validation messages on empty registration form', async ({ page }) => {
  await mockApi(page);
  await page.goto('/register');
  await page.getByTestId('register-submit').click();

  await expect(page.getByText("Le nom d'utilisateur est requis.")).toBeVisible();
  await expect(page.getByText('Entre une adresse email valide.')).toBeVisible();
  await expect(page.getByText(/Le mot de passe doit contenir au moins 6 caract/i)).toBeVisible();
});

test('registers a user and redirects to the dashboard', async ({ page }) => {
  await mockApi(page);
  await page.goto('/register');
  await page.getByTestId('register-username').fill('alice');
  await page.getByTestId('register-email').fill('alice@example.com');
  await page.getByTestId('register-password').fill('secret123');
  await page.getByTestId('register-submit').click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Projet E2E' })).toBeVisible();
  await expect(page.getByText('Verifier inscription', { exact: true })).toBeVisible();
});

test('prefills invited email and allows joining the project after registration', async ({ page }) => {
  await mockApi(page, {
    invitation: {
      token: 'invite-123',
      email: 'invitee@example.com',
      projectName: 'Projet Invitation',
      role: 'MEMBER',
      status: 'PENDING',
    },
  });

  await page.goto('/register?invitation=invite-123');

  await expect(page.getByText('Projet Invitation')).toBeVisible();
  await expect(page.getByTestId('register-email')).toHaveValue('invitee@example.com');
  await expect(page.getByTestId('register-email')).toHaveAttribute('readonly');

  await page.getByTestId('register-username').fill('invitee');
  await page.getByTestId('register-password').fill('secret123');
  await page.getByTestId('register-submit').click();

  await expect(page).toHaveURL(/\/invitation\/invite-123$/);
  await expect(page.getByRole('button', { name: 'Rejoindre le projet' })).toBeVisible();

  await page.getByRole('button', { name: 'Rejoindre le projet' }).click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('heading', { name: 'Projet Invitation' })).toBeVisible();
});

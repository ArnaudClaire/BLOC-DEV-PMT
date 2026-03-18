import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', async (route, request) => {
    const url = new URL(request.url());
    const { pathname } = url;

    if (pathname === '/api/users' && request.method() === 'POST') {
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({ id: 99, username: 'alice', email: 'alice@example.com' }),
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
            email: 'alice@example.com',
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
            name: 'Projet E2E',
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

    if (pathname === '/api/project-invitations' || pathname === '/api/notifications' || pathname === '/api/task-histories') {
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
});

test('shows validation messages on empty registration form', async ({ page }) => {
  await page.goto('/register');
  await page.getByTestId('register-submit').click();

  await expect(page.getByText("Le nom d'utilisateur est requis.")).toBeVisible();
  await expect(page.getByText('Entre une adresse email valide.')).toBeVisible();
  await expect(page.getByText('Le mot de passe doit contenir au moins 6 caracteres.')).toBeVisible();
});

test('registers a user and redirects to the dashboard', async ({ page }) => {
  await page.goto('/register');
  await page.getByTestId('register-username').fill('alice');
  await page.getByTestId('register-email').fill('alice@example.com');
  await page.getByTestId('register-password').fill('secret123');
  await page.getByTestId('register-submit').click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByText('Projet E2E')).toBeVisible();
  await expect(page.getByText('Verifier inscription')).toBeVisible();
});

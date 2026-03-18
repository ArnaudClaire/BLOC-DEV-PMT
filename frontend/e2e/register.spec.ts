import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.route('http://localhost:8080/users', async (route, request) => {
    if (request.method() === 'POST') {
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({ id: 99, username: 'alice', email: 'alice@example.com' }),
      });
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([{ id: 99, username: 'alice', email: 'alice@example.com' }]),
    });
  });

  await page.route('http://localhost:8080/projects', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify([{ id: 1, name: 'Projet E2E', description: 'Projet mocke' }]),
    });
  });

  await page.route('http://localhost:8080/tasks', async (route) => {
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
          projectId: 1,
          createdById: 99,
          assignedToId: 99,
          dueDate: '2026-03-20T00:00:00.000Z',
          endDate: '2026-03-20T00:00:00.000Z',
        },
      ]),
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

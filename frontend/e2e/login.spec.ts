import { expect, test } from '@playwright/test';

import { createMockState, installMockApi } from './support/mock-api';

test('logs in with email and password and opens the dashboard', async ({ page }) => {
  const state = createMockState();

  await installMockApi(page, state);
  await page.goto('/login');

  await page.getByTestId('login-email').fill('bob@example.com');
  await page.getByTestId('login-password').fill('secret123');
  await page.getByTestId('login-submit').click();

  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByTestId('project-filter-1')).toBeVisible();
  await expect(page.getByTestId('task-card-10')).toBeVisible();
});

import { expect, Page, test } from '@playwright/test';

import { MockState, createMockState, installMockApi, setSession } from './support/mock-api';

async function openDashboardAs(page: Page, state: MockState, userEmail: string): Promise<void> {
  const user = state.users.find((candidate) => candidate.email === userEmail);

  if (!user) {
    throw new Error(`Unknown mock user: ${userEmail}`);
  }

  await setSession(page, user);
  await installMockApi(page, state);
  await page.goto('/dashboard');
  await expect(page.getByTestId('dashboard-refresh')).toBeVisible();
}

test('admin can create a project, invite a member, filter history by project and update a member role', async ({ page }) => {
  const state = createMockState();

  await openDashboardAs(page, state, 'alice@example.com');

  await expect(page.getByTestId('history-401')).toBeVisible();
  await expect(page.getByTestId('history-402')).toBeVisible();

  await page.getByTestId('project-name').fill('Qualite Sprint');
  await page.locator('app-dashboard-project-form textarea').fill('Projet dedie a la stabilisation et a la qualite des livrables.');
  await page.getByTestId('project-start-date').fill('2026-04-01');
  await page.getByTestId('project-submit').click();

  await expect(page.getByTestId('project-filter-10')).toBeVisible();

  await page.getByTestId('project-filter-1').click();
  await expect(page.getByText('Projet courant:')).toBeVisible();
  await expect(page.getByTestId('history-401')).toBeVisible();
  await expect(page.getByTestId('history-402')).toHaveCount(0);

  await page.getByTestId('invitation-email').fill('invitee@example.com');
  await page.getByTestId('invitation-role').selectOption({ label: 'Observateur' });
  await page.getByTestId('invitation-submit').click();

  const invitationRow = page.locator('[data-testid^="invitation-row-"]').filter({ hasText: 'invitee@example.com' });
  await expect(invitationRow).toBeVisible();
  await expect(invitationRow).toContainText('Observateur');

  await page.getByTestId('member-role-101').selectOption({ label: 'Observateur' });
  await expect(page.getByTestId('member-row-101')).toContainText('Observateur');
});

test('member can create and assign a task, see it on the board, receive a notification and track history', async ({ page }) => {
  const state = createMockState();

  await openDashboardAs(page, state, 'bob@example.com');

  await page.getByTestId('task-title').fill('Preparer release');
  await page.locator('app-dashboard-task-form textarea').fill('Verifier la release candidate, la checklist et les validations finales.');
  await page.getByTestId('task-project').selectOption({ label: 'PMT Launch' });
  await page.getByTestId('task-priority').selectOption({ label: 'Haute' });
  await page.getByTestId('task-status').selectOption({ label: 'A faire' });
  await page.getByTestId('task-due-date').fill('2026-04-02');
  await page.getByTestId('task-assignee').selectOption({ label: 'bob.member' });
  await page.getByTestId('task-submit').click();

  await expect(page.locator('.kanban-column[data-tone="slate"]')).toContainText('Preparer release');
  await expect(page.locator('[data-testid^="notification-"]').filter({ hasText: 'Preparer release' })).toBeVisible();
  await expect(page.locator('[data-testid^="history-"]').filter({ hasText: 'Creation - tache' })).toBeVisible();
  await expect(page.locator('[data-testid^="history-"]').filter({ hasText: 'Assignation - assignation' })).toBeVisible();
});

test('member can update a task and add an end date from the task detail view', async ({ page }) => {
  const state = createMockState();

  await openDashboardAs(page, state, 'bob@example.com');

  await page.getByTestId('task-card-10').click();
  await expect(page.getByTestId('task-details-modal')).toBeVisible();

  await page.getByTestId('task-detail-status').selectOption({ label: 'Terminee' });
  await page.getByTestId('task-detail-end-date').fill('2026-03-21');
  await page.getByTestId('task-detail-submit').click();

  await expect(page.getByTestId('task-details-modal')).toHaveCount(0);
  await expect(page.locator('.kanban-column[data-tone="green"]')).toContainText('Configurer auth');
  await expect(page.getByText('Cloture - statut')).toBeVisible();
  await expect(page.getByText('Mise a jour - date de fin')).toBeVisible();
});

test('observer can open task details in read-only mode and only sees history from visible projects', async ({ page }) => {
  const state = createMockState();

  await openDashboardAs(page, state, 'claire@example.com');

  await expect(page.getByTestId('project-filter-1')).toBeVisible();
  await expect(page.getByTestId('project-filter-2')).toHaveCount(0);
  await expect(page.getByTestId('task-card-10')).toBeVisible();
  await expect(page.getByTestId('task-card-11')).toHaveCount(0);
  await expect(page.getByTestId('history-401')).toBeVisible();
  await expect(page.getByTestId('history-402')).toHaveCount(0);

  await page.getByTestId('task-card-10').click();

  await expect(page.getByTestId('task-details-modal')).toBeVisible();
  await expect(page.getByTestId('task-detail-readonly')).toBeVisible();
  await expect(page.getByTestId('task-detail-submit')).toHaveCount(0);
});

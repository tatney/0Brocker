import { expect, test } from '@playwright/test';

test.beforeEach(async ({ page }) => {
  const errors = [];
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  page.errors = errors;
  await page.goto('/');
  await expect(page.locator('.shell')).toBeVisible();
});

test('renders the overview with summary stats and no console errors', async ({ page }) => {
  await expect(page.locator('h1')).toHaveText('Overview');
  await expect(page.locator('.brand-text strong')).toHaveText('0Brocker');
  await expect(page.locator('.stat-card')).toHaveCount(6);
  expect(page.errors).toEqual([]);
});

test('shows the demo-mode notice when Supabase is unconfigured', async ({ page }) => {
  await expect(page.locator('.conn')).toHaveText(/Demo mode|Live/);
  await expect(page.locator('.banner').first()).toContainText('Supabase');
});

test('navigates to every workspace section', async ({ page }) => {
  const sections = [
    ['Bookings', /bookings/],
    ['Providers', /providers/],
    ['Listings', /listings/],
    ['People', /accounts/],
    ['Money', /transactions/],
    ['Reviews', /reviews/],
  ];

  for (const [label, subtitle] of sections) {
    await page.getByRole('button', { name: label, exact: true }).click();
    await expect(page.locator('h1')).toHaveText(label);
    await expect(page.locator('.page-header p')).toHaveText(subtitle);
    expect(page.errors).toEqual([]);
  }

  await expect(page).toHaveURL(/#\/reviews$/);
});

test('filters the listings table', async ({ page }) => {
  await page.getByRole('button', { name: 'Listings', exact: true }).click();
  await expect(page.locator('.table tbody tr')).toHaveCount(14);

  await page.getByPlaceholder('Search title, area or city…').fill('kololo');
  await expect(page.locator('.table tbody tr')).toHaveCount(1);
  await expect(page.locator('.table tbody tr')).toContainText('Commercial Plot in Kololo');

  await page.getByPlaceholder('Search title, area or city…').fill('');
  await expect(page.locator('.table tbody tr')).toHaveCount(14);
});

test('filters providers by category', async ({ page }) => {
  await page.getByRole('button', { name: 'Providers', exact: true }).click();
  await expect(page.locator('.table tbody tr')).toHaveCount(10);

  await page.getByLabel('Category', { exact: true }).selectOption('PLUMBING');
  await expect(page.locator('.table tbody tr')).toHaveCount(1);
  await expect(page.locator('.table tbody tr')).toContainText('Joseph Kiwanuka');
});

test('shows the review scoreboard for every dimension', async ({ page }) => {
  await page.getByRole('button', { name: 'Reviews', exact: true }).click();
  await expect(page.locator('.score')).toHaveCount(5);
  expect(page.errors).toEqual([]);
});

test('refuses to mutate bookings while Supabase is unconfigured', async ({ page }) => {
  await page.getByRole('button', { name: 'Bookings', exact: true }).click();
  await page.getByRole('button', { name: 'Confirm' }).first().click();
  await expect(page.locator('.banner-danger')).toContainText('not configured');
});

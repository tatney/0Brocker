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
  await expect(page.locator('.banner').first()).toContainText('Demo workspace');
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


test('advances bookings using the app workflow and persists the demo change', async ({ page }) => {
  await page.getByRole('button', { name: 'Bookings', exact: true }).click();
  await expect(page.locator('.table tbody tr').filter({hasText:'#1002'}).getByRole('button',{name:'Request payment'})).toHaveCount(0);
  const row = page.locator('.table tbody tr').filter({ hasText: '#1004' });
  await row.getByRole('button', { name: 'Mark en route', exact: true }).click();
  await expect(row).toContainText('en route');
  await page.reload();
  await expect(page.locator('.table tbody tr').filter({hasText:'#1004'})).toContainText('en route');
  expect(page.errors).toEqual([]);
});

test('verifies a demo listing and keeps the result on reload', async ({ page }) => {
  await page.getByRole('button', { name: 'Listings', exact: true }).click();
  const row=page.locator('.table tbody tr').filter({hasText:'Portion for Rent'});
  await row.getByRole('button',{name:'Verify',exact:true}).click();
  await expect(row).toContainText('Verified');
  await page.reload();
  await expect(page.locator('.table tbody tr').filter({hasText:'Portion for Rent'})).toContainText('Verified');
});

test('exports only filtered listing records', async ({ page }) => {
  await page.getByRole('button', { name: 'Listings', exact: true }).click();
  await page.getByRole('searchbox').fill('kololo');
  const downloaded=page.waitForEvent('download');
  await page.getByRole('button',{name:'Export CSV'}).click();
  const download=await downloaded;
  const {readFile}=await import('node:fs/promises');
  const csv=await readFile(await download.path(),'utf8');
  expect(csv).toContain('Commercial Plot in Kololo');
  expect(csv).not.toContain('Portion for Rent');
});

test('mobile navigation remains labeled and does not overflow the viewport', async ({ page }) => {
  await page.setViewportSize({width:375,height:812});
  await expect(page.getByRole('button',{name:'Bookings',exact:true})).toBeVisible();
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);
  await page.getByRole('button',{name:'Bookings',exact:true}).click();
  expect(await page.evaluate(()=>document.documentElement.scrollWidth<=window.innerWidth)).toBe(true);
});

test('captures desktop and mobile dashboard previews', async ({ page }) => {
  await expect(page.locator('.stat-card')).toHaveCount(6);
  await page.screenshot({path:'test-results/dashboard-desktop.png',fullPage:true});
  await page.setViewportSize({width:375,height:812});
  await page.screenshot({path:'test-results/dashboard-mobile.png',fullPage:true});
});

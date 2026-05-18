import { test, expect } from '@playwright/test';

test('landing page opens and auth actions are visible', async ({ page }) => {
  await page.goto(process.env.BASE_URL || 'http://localhost:8080/landing.html');
  await expect(page.locator('body')).toContainText('Eduvibe');
  await expect(page.locator('a[href="login.html"]').first()).toBeVisible();
});

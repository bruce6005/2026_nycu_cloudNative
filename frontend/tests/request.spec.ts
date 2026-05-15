import { test, expect } from '@playwright/test';

test('應該能正確看到建立委託單頁面的標題', async ({ page }) => {
  // 假的身分資訊
  const mockUser = {
    id: 999,
    name: 'Test Agent',
    email: 'test@example.com',
    role: 'REQUESTER',
    managerId: 1
  };

  // 2. 攔截 API 請求
  await page.route('**/api/auth/google', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ user: mockUser }) });
  });

  await page.route('**/api/recipes', route => route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
  await page.route('**/api/request', route => route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));

  // 3. 前往網頁，寫入 LocalStorage
  await page.addInitScript((user) => {
    localStorage.setItem('auth_user', JSON.stringify(user));
    localStorage.setItem('current_page', 'request');
  }, mockUser);
  await page.goto('/');

  // 4. 驗證 (使用 getByRole 避免重複元素衝突)
  // 檢查標題
  await expect(page.getByRole('heading', { name: '建立委託單', level: 2 })).toBeVisible({ timeout: 15000 });

  // 檢查導覽列是否顯示了我們注入的使用者名稱
  await expect(page.getByText('Test Agent')).toBeVisible();

  // 記錄結果
  await page.screenshot({ path: 'test-final-success.png' });
});

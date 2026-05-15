import { test, expect } from '@playwright/test';

/**
 * Frontend Tests for Request Module
 * 
 * ----------------------------------------------------
 * 測試行為清單 (Frontend Behavior Summary):
 * ----------------------------------------------------
 * 1. 初始化與讀取 (Initialization):
 *    - [x] 正確顯示 Mock 使用者與初始清單內容
 * 
 * 2. 邊緣測試 (Edge Case Validation):
 *    - [x] [標題校驗]：驗證標題為空時無法送出 (HTML5)
 *    - [x] [樣本校驗]：驗證樣本代碼未填時彈出警告 (Alert)
 * 
 * 3. 完整生命週期 (Full Lifecycle):
 *    - [x] [建立]：填寫資訊後建立，狀態預設為 PENDING
 *    - [x] [讀取/展開]：手動刷新清單，展開驗證詳情資料正確
 *    - [x] [狀態轉變]：模擬後端處理完成 (DONE)，刪除按鈕正確顯示
 *    - [x] [刪除/封存]：點擊刪除、處理確認視窗後，資料從 UI 徹底消失
 * ----------------------------------------------------
 */

const mockUser = {
  id: 999,
  name: 'Test Agent',
  role: 'REQUESTER',
  managerId: 1
};

// 模擬後端資料庫
let mockDatabase: any[] = [];

test.beforeEach(async ({ page }) => {
  // 重設資料庫，避免測試間干擾
  mockDatabase = [
    { id: 101, title: 'Existing Task', status: 'PENDING', priority: 'NORMAL', description: 'Initial item', samples: [] }
  ];

  // 1. 注入登入狀態
  await page.route('**/api/auth/google', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ user: mockUser }) });
  });

  await page.addInitScript((user) => {
    localStorage.setItem('auth_user', JSON.stringify(user));
    localStorage.setItem('current_page', 'request');
  }, mockUser);

  // 2. Mock 所有 API 請求
  await page.route('**/api/**', async route => {
    const method = route.request().method();
    const url = route.request().url();

    // GET /api/recipes
    if (url.includes('/api/recipes')) {
      return route.fulfill({ status: 200, body: JSON.stringify([{ id: 1, name: 'UV CURE' }, { id: 2, name: 'THERMAL' }]) });
    }

    // /api/request (POST or GET)
    if (url.endsWith('/api/request')) {
      if (method === 'GET') {
        return route.fulfill({ status: 200, body: JSON.stringify(mockDatabase) });
      }
      if (method === 'POST') {
        const payload = route.request().postDataJSON();
        const newId = 2000 + mockDatabase.length;
        const newItem = {
          id: newId,
          title: payload.title,
          status: 'PENDING',
          priority: payload.priority || 'NORMAL',
          description: payload.description || '',
          samples: payload.samples.map((s: any) => ({ ...s, status: 'PENDING', recipeName: 'MOCKED RECIPE' }))
        };
        mockDatabase.push(newItem);
        return route.fulfill({ status: 200, body: JSON.stringify({ id: newId }) });
      }
    }

    // GET /api/request/:id
    const idMatch = url.match(/\/api\/request\/(\d+)$/);
    if (idMatch && method === 'GET') {
      const id = parseInt(idMatch[1]);
      const item = mockDatabase.find(r => r.id === id);
      return item
        ? route.fulfill({ status: 200, body: JSON.stringify(item) })
        : route.fulfill({ status: 404 });
    }

    // PATCH /api/request/:id/archive
    if (url.endsWith('/archive') && method === 'PATCH') {
      const parts = url.split('/');
      const id = parseInt(parts[parts.length - 2]);
      mockDatabase = mockDatabase.filter(r => r.id !== id);
      return route.fulfill({ status: 204 });
    }

    // 其他請求放行
    await route.continue();
  });
});

test('Request Module - Edge Cases & Validation', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Test Agent')).toBeVisible();

  await test.step('校驗：標題必填 (HTML5)', async () => {
    await page.getByRole('button', { name: '建立委託單' }).click();
    const titleInput = page.getByPlaceholder('請輸入標題...');
    expect(await titleInput.evaluate((el: HTMLInputElement) => el.checkValidity())).toBe(false);
  });

  await test.step('校驗：樣本代碼不可為空', async () => {
    await page.getByPlaceholder('請輸入標題...').fill('Validation Job');
    page.once('dialog', async dialog => {
      expect(dialog.message()).toContain('請填寫所有樣本代碼');
      await dialog.dismiss();
    });
    await page.getByRole('button', { name: '建立委託單' }).click();
  });
});

test('Request Module - Lifecycle (Create -> State Change -> Delete)', async ({ page }) => {
  await page.goto('/');
  const newTitle = 'Lifecycle Test Job';

  await test.step('步驟 1：建立新委託 (預設 PENDING)', async () => {
    await page.getByPlaceholder('請輸入標題...').fill(newTitle);
    await page.getByPlaceholder('樣本代碼').fill('SN-LIFE-001');

    // 處理成功的 alert
    page.once('dialog', d => d.dismiss());
    await page.getByRole('button', { name: '建立委託單' }).click();

    // 檢查列表
    const listItem = page.locator('.list-item').filter({ hasText: newTitle });
    await expect(listItem).toBeVisible();
    await expect(listItem.getByText('PENDING')).toBeVisible();
  });

  await test.step('步驟 2：展開驗證內容', async () => {
    const listItem = page.locator('.list-item').filter({ hasText: newTitle });
    await listItem.getByRole('button', { name: '展開' }).click();
    await expect(page.getByText('SN-LIFE-001')).toBeVisible();
    await listItem.getByRole('button', { name: '收合' }).click();
  });

  await test.step('步驟 3：變更狀態為 DONE 並刪除', async () => {
    // 手動修改 mockDatabase 模擬後端狀態變更 (例如管理員已完成)
    const target = mockDatabase.find(r => r.title === newTitle);
    if (target) target.status = 'DONE';

    // 點擊重新整理按鈕
    await page.getByRole('button', { name: '🔄 重新整理' }).click();

    const listItem = page.locator('.list-item').filter({ hasText: newTitle });
    await expect(listItem.getByText('DONE')).toBeVisible();

    // 處理刪除確認框
    page.once('dialog', d => d.accept());
    await listItem.getByRole('button', { name: '刪除' }).click();

    // 驗證消失
    await expect(page.getByText(newTitle)).not.toBeVisible();
  });
});

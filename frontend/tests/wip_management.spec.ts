import { test, expect } from '@playwright/test';

/**
 * WIP Management 測試腳本 - 完整行為驗證版
 * 
 * ----------------------------------------------------
 * 測試行為清單 (Behavior Summary):
 * ----------------------------------------------------
 * 1. 初始化與權限 (Initialization):
 *    - [x] 正確注入 LAB_STAFF 身分，並於頂部顯示 Machine Operator Agent
 * 
 * 2. 導航功能 (Navigation):
 *    - [x] [側邊欄切換]：從預設頁面成功手動切換至 WIP Management 模組
 *    - [x] [頁面識別]：確認 WIP Pipeline 標題正確顯示
 * 
 * 3. 完整生命週期與位置變動 (Batch Lifecycle):
 *    - [x] [啟動任務]：選中 Queued Batch 並啟動，驗證其移動至 In Progress 區
 *    - [x] [完工驗證]：模擬後端完工後，驗證任務移動至 Finished Tasks 區
 * ----------------------------------------------------
 */

const mockUser = {
  id: 999,
  name: 'Machine Operator Agent',
  role: 'LAB_STAFF',
  managerId: 1
};

let mockWipDatabase: any[] = [];

test.beforeEach(async ({ page }) => {
  mockWipDatabase = [
    { 
      id: 101, status: 'QUEUED', equipmentName: 'UV-CURE-01', recipeName: 'STD_UV',
      sampleBarcodes: ['SN-001'], createTime: new Date().toISOString() 
    }
  ];

  // 1. 注入登入狀態 (完全保留你剛才成功的版本)
  await page.route('**/src/App.tsx*', async route => {
    const response = await route.fetch();
    let text = await response.text();
    const patchedText = text.replace(/useState(<[^>]*>)?\(\s*null\s*\)/, `useState(${JSON.stringify(mockUser)})`);
    await route.fulfill({ response, body: patchedText });
  });

  // 2. Mock API
  await page.route('**/api/**', async route => {
    const url = route.request().url();
    const method = route.request().method();

    if (url.includes('/api/wip_management')) {
      if (method === 'GET' && !url.includes('/start')) {
        return route.fulfill({ status: 200, body: JSON.stringify(mockWipDatabase) });
      }
      if (url.includes('/start') && method === 'PATCH') {
        const id = parseInt(url.split('/').slice(-2, -1)[0]);
        const item = mockWipDatabase.find(b => b.id === id);
        if (item) item.status = 'RUNNING';
        return route.fulfill({ status: 200, body: JSON.stringify(item || {}) });
      }
    }
    if (url.includes('/api/recipes')) return route.fulfill({ status: 200, body: JSON.stringify([]) });
    
    await route.continue();
  });
});

test('WIP Management - Navigation & Lifecycle Flow', async ({ page }) => {
  await page.goto('/');

  // 1. 驗證登入成功
  await expect(page.getByText('Machine Operator Agent')).toBeVisible({ timeout: 10000 });

  // 2. 執行導航與基礎檢查
  await test.step('操作：側邊欄導航', async () => {
    const navItem = page.locator('.menu-item').filter({ hasText: 'WIP Management' });
    await expect(navItem).toBeVisible();
    await navItem.click();
    await expect(page.getByText('WIP Pipeline')).toBeVisible();
    await expect(page.getByText('Batch #101')).toBeVisible();
  });

  // 3. 啟動任務流程測試
  await test.step('狀態變更：啟動 Queued 任務', async () => {
    // 點擊 Batch 卡片
    await page.locator('.wip-card').filter({ hasText: 'Batch #101' }).click();
    // 點擊啟動按鈕
    await page.getByRole('button', { name: 'START SCANNING & RUN' }).click();

    // 驗證 Batch 移動到 In Progress 區域
    const inProgressBox = page.locator('.card').filter({ hasText: 'In Progress' });
    await expect(inProgressBox.getByText('Batch #101')).toBeVisible();
  });

  // 4. 完成任務流程測試
  await test.step('狀態變更：任務完工並歸檔', async () => {
    // 模擬後端數據變更
    const b101 = mockWipDatabase.find(b => b.id === 101);
    if (b101) b101.status = 'FINISHED';

    // 重新載入並再次導航
    await page.reload();
    await page.locator('.menu-item').filter({ hasText: 'WIP Management' }).click();

    // 驗證移至 Finished Tasks
    const finishedBox = page.locator('.card').filter({ hasText: 'Finished Tasks' });
    await expect(finishedBox.getByText('Batch #101')).toBeVisible();
  });
});

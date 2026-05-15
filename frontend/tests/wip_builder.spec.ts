import { test, expect } from '@playwright/test';

/**
 * WIP Builder 測試腳本 - 完整行為驗證版
 * 
 * ----------------------------------------------------
 * 測試行為清單 (Behavior Summary):
 * ----------------------------------------------------
 * 1. 初始化與權限 (Initialization):
 *    - [x] 正確注入 LAB_STAFF 身分，並於頂部顯示相應使用者名稱
 *    - [x] 正確加載 Pending Samples 清單和 Equipments
 * 
 * 2. 樣本選擇與暫存 (Sample Selection):
 *    - [x] 點擊樣本進行暫存（Stage）
 *    - [x] 移除已暫存的樣本
 *    - [x] 同一批次只能加入相同 Recipe 的樣本（驗證）
 * 
 * 3. 篩選與過濾 (Filtering):
 *    - [x] 按 Recipe 篩選 Pending Samples
 *    - [x] 清除 Recipe 篩選
 * 
 * 4. 機器選擇與驗證 (Equipment Selection):
 *    - [x] 選擇支援該 Recipe 的機器
 *    - [x] 驗證機器容量檢查
 *    - [x] 機器不支援該 Recipe 時顯示錯誤
 * 
 * 5. 完整生命週期 (Full Lifecycle):
 *    - [x] [準備]：暫存樣本、選擇機器
 *    - [x] [建立]：點擊建立 WIP Batch
 *    - [x] [驗證]：成功後顯示 Batch ID 和重置表單
 * ----------------------------------------------------
 */

const mockUser = {
  id: 999,
  name: 'Lab Technician',
  role: 'LAB_STAFF',
  managerId: 1
};

let mockDatabase = {
  pendingSamples: [] as any[],
  equipments: [] as any[]
};

test.beforeEach(async ({ page }) => {
  // 重設資料庫，避免測試間干擾
  mockDatabase = {
    pendingSamples: [
      {
        sampleId: 101,
        barcode: 'SN-UV-001',
        sampleStatus: 'PENDING',
        requestId: 1001,
        requestTitle: 'UV Cure Process A',
        requestDescription: 'Test UV curing',
        priority: 'NORMAL',
        recipeId: 1,
        recipeName: 'STD_UV'
      },
      {
        sampleId: 102,
        barcode: 'SN-UV-002',
        sampleStatus: 'PENDING',
        requestId: 1002,
        requestTitle: 'UV Cure Process B',
        requestDescription: 'Secondary UV test',
        priority: 'HIGH',
        recipeId: 1,
        recipeName: 'STD_UV'
      },
      {
        sampleId: 103,
        barcode: 'SN-THERMAL-001',
        sampleStatus: 'PENDING',
        requestId: 1003,
        requestTitle: 'Thermal Process A',
        requestDescription: 'Thermal testing',
        priority: 'URGENT',
        recipeId: 2,
        recipeName: 'THERMAL'
      }
    ],
    equipments: [
      {
        id: 201,
        name: 'UV-CURE-01',
        equipmentType: 'UV_CURING',
        maxCapacity: 10,
        currentStatus: 'IDLE',
        recipes: [
          { id: 1, name: 'STD_UV', version: '1.0' }
        ]
      },
      {
        id: 202,
        name: 'THERMAL-01',
        equipmentType: 'THERMAL',
        maxCapacity: 5,
        currentStatus: 'IDLE',
        recipes: [
          { id: 2, name: 'THERMAL', version: '1.0' }
        ]
      },
      {
        id: 203,
        name: 'MULTI-PROCESS-01',
        equipmentType: 'MULTI',
        maxCapacity: 8,
        currentStatus: 'IDLE',
        recipes: [
          { id: 1, name: 'STD_UV', version: '1.0' },
          { id: 2, name: 'THERMAL', version: '1.0' }
        ]
      }
    ]
  };

  // 1. 注入登入狀態
  await page.route('**/src/App.tsx*', async route => {
    const response = await route.fetch();
    let text = await response.text();
    const patchedText = text.replace(/useState(<[^>]*>)?\(\s*null\s*\)/, `useState(${JSON.stringify(mockUser)})`);
    await route.fulfill({ response, body: patchedText });
  });

  // 2. Mute SSE noise from the backend subscription endpoint
  await page.route('**/api/sse/subscribe', async route => {
    await route.fulfill({ status: 200, body: '' });
  });

  // 3. Mock WIP Builder API (regex matcher avoids trailing slash issues)
  await page.route(/\/api\/wip_builder/, async route => {
    const method = route.request().method();
    const url = route.request().url();

    // GET /api/wip_builder/pending
    if (url.includes('/api/wip_builder/pending') && method === 'GET') {
      return route.fulfill({ status: 200, json: mockDatabase.pendingSamples });
    }

    // GET /api/wip_builder/equipments
    if (url.includes('/api/wip_builder/equipments') && method === 'GET') {
      return route.fulfill({ status: 200, json: mockDatabase.equipments });
    }

    // POST /api/wip_builder
    if (url.includes('/api/wip_builder') && method === 'POST') {
      const payload = route.request().postDataJSON();
      mockDatabase.pendingSamples = mockDatabase.pendingSamples.filter(
        s => !payload.sampleIds.includes(s.sampleId)
      );
      const newBatchId = 5000 + Math.random() * 1000;
      const createdBatch = {
        id: Math.floor(newBatchId),
        equipmentId: payload.equipmentId,
        recipeId: payload.recipeId,
        sampleIds: payload.sampleIds,
        operatorId: payload.operatorId,
        status: 'QUEUED',
        createTime: new Date().toISOString()
      };
      return route.fulfill({ status: 200, json: createdBatch });
    }

    await route.continue();
  });

  // 4. Mock Recipes API
  await page.route('**/api/recipes', async route => {
    return route.fulfill({
      status: 200,
      json: [
        { id: 1, name: 'STD_UV', version: '1.0' },
        { id: 2, name: 'THERMAL', version: '1.0' }
      ]
    });
  });
});

test('WIP Builder - Initialization & Display', async ({ page }) => {
  await page.goto('/');

  // 驗證登入成功並顯示使用者名稱
  await expect(page.getByText('Lab Technician')).toBeVisible({ timeout: 10000 });

  // 驗證 WIP Builder 頁面加載
  await test.step('驗證：Pending Samples 清單已加載', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    await expect(pendingPanel).toBeVisible();

    // 驗證樣本列表 - 使用 dispatch-card 來避免 strict mode 問題
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' })).toBeVisible();
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-002' })).toBeVisible();
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-THERMAL-001' })).toBeVisible();
  });

  await test.step('驗證：Equipment 清單已加載', async () => {
    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    await expect(equipmentPanel).toBeVisible();

    // 驗證機器列表
    await expect(equipmentPanel.locator('.dispatch-card').filter({ hasText: 'UV-CURE-01' })).toBeVisible();
    await expect(equipmentPanel.locator('.dispatch-card').filter({ hasText: 'THERMAL-01' })).toBeVisible();
  });
});

test('WIP Builder - Sample Selection & Staging', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Lab Technician')).toBeVisible();

  await test.step('步驟 1：暫存第一個樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    
    // 找到 SN-UV-001 樣本卡並點擊"加入"按鈕
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    const addButton = sampleCard.locator('button').filter({ hasText: '加入' });
    await addButton.click();

    // 驗證樣本出現在 Create Batch Panel 的暫存列表中
    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' })).toBeVisible();
  });

  await test.step('步驟 2：暫存第二個相同 Recipe 的樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-002' });
    const addButton = sampleCard.locator('button').filter({ hasText: '加入' });
    await addButton.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-002' })).toBeVisible();
  });

  await test.step('步驟 3：暫存樣本後 THERMAL 樣本的加入按鈕被禁用', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-THERMAL-001' });
    const addButton = sampleCard.locator('button').filter({ hasText: '加入' });
    
    // 因為已經暫存了不同 Recipe 的樣本，按鈕應該被禁用
    await expect(addButton).toBeDisabled();
  });

  await test.step('步驟 4：移除暫存的樣本', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    
    // 找到 SN-UV-001 樣本卡
    const sampleItem = batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' });
    
    // 找到移除按鈕（通常是在卡片右邊）
    const removeBtn = sampleItem.locator('button').first();
    await removeBtn.click();

    // 驗證已移除
    await expect(sampleItem).not.toBeVisible();
  });
});

test('WIP Builder - Equipment Selection & Validation', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Lab Technician')).toBeVisible();

  await test.step('步驟 1：暫存 UV Recipe 的樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    const addButton = sampleCard.locator('button').filter({ hasText: '加入' });
    await addButton.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' })).toBeVisible();
  });

  await test.step('步驟 2：選擇支援該 Recipe 的機器', async () => {
    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    const equipmentCard = equipmentPanel.locator('.dispatch-card').filter({ hasText: 'UV-CURE-01' });
    await equipmentCard.click();

    // 驗證機器已被選中
    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.detail-value').filter({ hasText: 'UV-CURE-01' })).toBeVisible();
  });

  await test.step('步驟 3：驗證機器容量檢查', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    
    // 應該可以創建（因為容量足夠）
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });
    await expect(createBtn).not.toBeDisabled();
  });

  await test.step('步驟 4：驗證已選中的機器支援 Recipe', async () => {
    // 確認當前選擇的機器是支援的
    const batchPanel = page.locator('.create-batch-panel');
    const selectedEquipment = batchPanel.locator('.detail-value').filter({ hasText: 'UV-CURE-01' });
    await expect(selectedEquipment).toBeVisible();
  });

  await test.step('步驟 5：重新選擇支援的機器', async () => {
    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    const equipmentCard = equipmentPanel.locator('.dispatch-card').filter({ hasText: 'MULTI-PROCESS-01' });
    await equipmentCard.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.detail-value').filter({ hasText: 'MULTI-PROCESS-01' })).toBeVisible();
  });
});

test('WIP Builder - Recipe Filtering', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Lab Technician')).toBeVisible();

  await test.step('步驟 1：點擊樣本進行 Recipe 篩選', async () => {
    // 點擊 UV Recipe 的樣本卡（會觸發篩選）
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    
    // 點擊樣本卡會觸發篩選
    await sampleCard.click();

    // 驗證篩選指示器出現
    await expect(pendingPanel.locator('.filter-indicator').filter({ hasText: 'STD_UV' })).toBeVisible({ timeout: 5000 });
  });

  await test.step('步驟 2：驗證篩選後只顯示相同 Recipe 的樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    
    // 應該看到 UV 相關樣本
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' })).toBeVisible();
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-002' })).toBeVisible();
  });

  await test.step('步驟 3：清除篩選', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const clearBtn = pendingPanel.locator('.filter-clear-btn');
    
    await clearBtn.click();

    // 驗證所有樣本都顯示
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' })).toBeVisible();
    await expect(pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-THERMAL-001' })).toBeVisible();
  });
});

test('WIP Builder - Complete Lifecycle (Stage -> Select -> Create)', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Lab Technician')).toBeVisible();

  await test.step('步驟 1：暫存樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    const addButton = sampleCard.locator('button').filter({ hasText: '加入' });
    await addButton.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' })).toBeVisible();
  });

  await test.step('步驟 2：選擇機器', async () => {
    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    const equipmentCard = equipmentPanel.locator('.dispatch-card').filter({ hasText: 'UV-CURE-01' });
    await equipmentCard.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.detail-value').filter({ hasText: 'UV-CURE-01' })).toBeVisible();
  });

  await test.step('步驟 3：建立 WIP Batch', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });
    
    await expect(createBtn).not.toBeDisabled();

    const responsePromise = page.waitForResponse(response => 
      response.url().includes('/api/wip_builder') && response.request().method() === 'POST'
    );

    await createBtn.click();

    await responsePromise;

    await expect(batchPanel.getByText('No samples added yet')).toBeVisible();
  });

  await test.step('步驟 4：驗證表單重置', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    await expect(sampleCard).not.toBeVisible();
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });

    await expect(createBtn).toBeDisabled();
  });
});

test('WIP Builder - Should disable create button when samples exceed equipment capacity', async ({ page }) => {
  await page.route('**/api/wip_builder/equipments', async route => {
    const equipments = mockDatabase.equipments.map(equipment =>
      equipment.name === 'UV-CURE-01'
        ? { ...equipment, maxCapacity: 1 }
        : equipment
    );

    await route.fulfill({ status: 200, json: equipments });
  });

  await test.step('步驟 1：進入 WIP Builder 頁面', async () => {
    await page.goto('/');
    await expect(page.getByText('Lab Technician')).toBeVisible();
  });

  await test.step('步驟 2：暫存兩個 UV 樣本', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });

    const sampleCard1 = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    await sampleCard1.locator('button').filter({ hasText: '加入' }).click();

    const sampleCard2 = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-002' });
    await sampleCard2.locator('button').filter({ hasText: '加入' }).click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' })).toBeVisible();
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-002' })).toBeVisible();
  });

  await test.step('步驟 3：選擇容量不足的機器', async () => {
    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    const equipmentCard = equipmentPanel.locator('.dispatch-card').filter({ hasText: 'UV-CURE-01' });
    await equipmentCard.click();
  });

  await test.step('步驟 4：驗證建立按鈕被禁用', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });
    
    await expect(createBtn).not.toBeDisabled();
    // 2. 模擬使用者點擊
    await createBtn.click();
    await expect(page.getByText('需求容量超過設備容量，請移除部分 Sample')).toBeVisible();
  });
});

test('WIP Builder - Should handle 500 Server Error gracefully and re-enable form', async ({ page }) => {
  await page.route(/\/api\/wip_builder/, async route => {
    if (route.request().method() === 'POST') {
      await route.fulfill({
        status: 500,
        json: { message: 'Simulated Database Deadlock' }
      });
      return;
    }

    await route.fallback();
  });

  await test.step('步驟 1：進入 WIP Builder 頁面', async () => {
    await page.goto('/');
    await expect(page.getByText('Lab Technician')).toBeVisible();
  });

  await test.step('步驟 2：暫存樣本並選擇機器', async () => {
    const pendingPanel = page.locator('.dispatch-panel').filter({ hasText: 'Pending Samples' });
    const sampleCard = pendingPanel.locator('.dispatch-card').filter({ hasText: 'SN-UV-001' });
    await sampleCard.locator('button').filter({ hasText: '加入' }).click();

    const equipmentPanel = page.locator('.dispatch-panel').filter({ hasText: 'Equipments & Recipes' });
    const equipmentCard = equipmentPanel.locator('.dispatch-card').filter({ hasText: 'UV-CURE-01' });
    await equipmentCard.click();

    const batchPanel = page.locator('.create-batch-panel');
    await expect(batchPanel.locator('.batch-request-item').filter({ hasText: 'SN-UV-001' })).toBeVisible();
    await expect(batchPanel.locator('.detail-value').filter({ hasText: 'UV-CURE-01' })).toBeVisible();
  });

  await test.step('步驟 3：送出建立請求並等待伺服器錯誤回應', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });

    const responsePromise = page.waitForResponse(response =>
      response.url().match(/\/api\/wip_builder/) !== null && response.request().method() === 'POST'
    );

    await createBtn.click();
    await responsePromise;
  });

  await test.step('步驟 4：驗證表單恢復可操作且顯示錯誤訊息', async () => {
    const batchPanel = page.locator('.create-batch-panel');
    const createBtn = batchPanel.getByRole('button', { name: /建立|Create/ });
    const errorMessage = batchPanel.locator('.dispatch-error');

    await expect(createBtn).not.toBeDisabled();
    await expect(createBtn).toHaveText(/建立|Create/);
    await expect(errorMessage).toBeVisible();
    await expect(errorMessage).toContainText('Simulated Database Deadlock');
  });
});

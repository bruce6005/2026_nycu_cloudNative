import { test, expect } from '@playwright/test'

const mockUser = {
  id: 999,
  name: 'Test Agent',
  role: 'MANAGER',
  managerId: 1
};

let mockDatabase: any[] = [];

test.beforeEach(async ({ page }) => {
  mockDatabase = [
    {
      id: 300,
      title: 'Approval Task 1',
      factoryUserId: 10,
      approverId: 999,
      priority: 'NORMAL',
      status: 'PENDING',
      description: 'Please approve',
      createTime: new Date().toISOString(),
      samples: [{ barcode: 'S-100', recipeName: 'R-A' }]
    }
  ];

  // 注入登入狀態 (使用 Context + localStorage)
  await page.route('**/api/auth/google', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ user: mockUser }) });
  });

  await page.addInitScript((user) => {
    localStorage.setItem('auth_user', JSON.stringify(user));
    localStorage.setItem('current_page', 'approval');
  }, mockUser);

  // Mock Approval API
  await page.route('**/approval/**', async route => {
    const method = route.request().method();
    const url = route.request().url();

    // GET pending
    if (url.includes('/approval/pending') && method === 'GET') {
      const params = new URL(url).searchParams;
      const approverId = Number(params.get('approverId'));
      const data = mockDatabase.filter(d => d.approverId === approverId && d.status === 'PENDING');
      return route.fulfill({ status: 200, body: JSON.stringify(data) });
    }

    // POST /approval/:id
    const postMatch = url.match(/\/approval\/(\d+)$/);
    if (postMatch && method === 'POST') {
      const id = Number(postMatch[1]);
      const payload = route.request().postDataJSON();
      const item = mockDatabase.find(d => d.id === id);
      if (item) {
        if (payload.action === 'APPROVE') item.status = 'APPROVED';
        if (payload.action === 'REJECT') item.status = 'REJECTED';
        return route.fulfill({ status: 200 });
      }
      return route.fulfill({ status: 404 });
    }

    await route.continue();
  });
});

test('Approval Module - Load and Approve', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByText('Test Agent')).toBeVisible();
  // 切換到 Approval 分頁（若尚未顯示）
  const approvalMenu = page.getByText('Approval');
  if (await approvalMenu.count() > 0) {
    await approvalMenu.first().click();
  }

  // 等待列表載入並選中
  const card = page.locator('.order-card').filter({ hasText: 'Approval Task 1' });
  await expect(card).toBeVisible();
  await card.click();

  // 詳細資料顯示
  await expect(page.getByText('委託單詳情 - #300')).toBeVisible();
  await expect(page.getByText('S-100')).toBeVisible();

  // 點擊 Approve
  await page.getByRole('button', { name: 'Approve' }).click();

  // 觸發後端 mock 更新，按鈕處理後列表應變空，詳細區顯示預設訊息
  await page.waitForTimeout(200); // 等待 loadData 完成
  await expect(page.getByText('請從左側選擇要審核的委託單')).toBeVisible();
});

test('Approval Module - Reject flow', async ({ page }) => {
  await page.goto('/');
  // 切換到 Approval 分頁（若尚未顯示）
  const approvalMenu2 = page.getByText('Approval');
  if (await approvalMenu2.count() > 0) {
    await approvalMenu2.first().click();
  }
  const card = page.locator('.order-card').filter({ hasText: 'Approval Task 1' });
  await expect(card).toBeVisible();
  await card.click();

  // 點擊 Reject，輸入理由並送出
  await page.getByRole('button', { name: 'Reject' }).click();
  const textarea = page.getByPlaceholder('Enter reject reason...');
  await textarea.fill('Not acceptable');
  await page.getByRole('button', { name: 'Submit' }).click();

  // 等待 loadData 並驗證詳細區回到預設
  await page.waitForTimeout(200);
  await expect(page.getByText('請從左側選擇要審核的委託單')).toBeVisible();
});

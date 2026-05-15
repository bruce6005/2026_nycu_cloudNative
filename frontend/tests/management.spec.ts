import { test, expect } from '@playwright/test';

const mockManager = {
  id: 1,
  name: 'Manager Admin',
  email: 'manager@example.com',
  role: 'MANAGER',
  managerId: null
};

test.describe('Management Flows', () => {

  test.beforeEach(async ({ page }) => {
    await page.route('**/api/auth/google', async route => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ user: mockManager }) });
    });
  });

  test('Equipment Type Management Flow', async ({ page }) => {
    const mockSchema = {
      id: 1,
      equipmentType: 'Oven',
      parameterSchema: JSON.stringify({
        type: 'object',
        properties: { temp: { type: 'number' } },
        required: ['temp']
      })
    };

    // 1. Initially return empty list
    await page.route('**/api/equipment-schemas', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (route.request().method() === 'POST') {
        // Return created schema
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockSchema) });
      } else {
        await route.continue();
      }
    });

    await page.addInitScript((user) => {
      localStorage.setItem('auth_user', JSON.stringify(user));
      localStorage.setItem('current_page', 'equipmentTypes');
    }, mockManager);
    await page.goto('/');

    // 2. Click + New Type
    await page.getByRole('button', { name: '+ New Type' }).click();

    // 3. Fill Form
    await page.getByPlaceholder('Equipment Type Name').fill('Oven');
    await page.getByPlaceholder('Parameter Name').fill('temp');
    await page.getByRole('combobox').selectOption('number'); // type select
    await page.getByLabel('Required').check();

    // 4. intercept next GET to return the list with new item
    // Since page.route overrides previous ones with the same pattern, we do it here.
    await page.route('**/api/equipment-schemas', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockSchema]) });
      } else if (route.request().method() === 'POST') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockSchema) });
      }
    });

    await page.getByRole('button', { name: 'Save Type' }).click();

    // 5. Verify UI
    await expect(page.getByText('Oven', { exact: true })).toBeVisible();
    await expect(page.getByText('temp', { exact: true })).toBeVisible();

    // 6. Test Delete (Bound to equipment)
    await page.route('**/api/equipment-schemas/1', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({ status: 400, contentType: 'application/json', body: JSON.stringify({ message: "foreign key constraint fails" }) });
      }
    });

    let alertTriggered = false;
    const boundDialogHandler = async (dialog: any) => {
      if (dialog.type() === 'confirm') {
        await dialog.accept();
      } else if (dialog.type() === 'alert') {
        expect(dialog.message()).toContain('Cannot delete');
        alertTriggered = true;
        await dialog.accept();
      }
    };
    page.on('dialog', boundDialogHandler);
    await page.getByRole('button', { name: 'Delete' }).click();
    await page.waitForTimeout(500); // give time for the alert
    expect(alertTriggered).toBe(true);
    page.removeListener('dialog', boundDialogHandler);

    // 7. Test Delete (Unbound / Success)
    await page.route('**/api/equipment-schemas/1', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({ status: 200 });
      }
    });
    // mock GET to return empty after delete
    await page.route('**/api/equipment-schemas', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else {
        await route.continue();
      }
    });

    const unboundDialogHandler = async (dialog: any) => {
      if (dialog.type() === 'confirm') await dialog.accept();
    };
    page.on('dialog', unboundDialogHandler);
    await page.getByRole('button', { name: 'Delete' }).click();
    await expect(page.getByText('Oven', { exact: true })).toBeHidden();
    page.removeListener('dialog', unboundDialogHandler);
  });

  test('Equipment Flow', async ({ page }) => {
    const mockSchema = {
      id: 1,
      equipmentType: 'Oven',
      parameterSchema: '{}'
    };

    const mockEq = {
      id: 1,
      name: 'Oven-Alpha',
      equipmentType: 'Oven',
      equipmentTypeSchemaId: 1,
      maxCapacity: 100,
      currentStatus: 'IDLE',
      recipes: []
    };

    await page.route('**/api/equipment-schemas', route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockSchema]) }));

    await page.route('**/api/wip_builder/equipments', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      }
    });

    await page.route('**/api/equipments', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockEq) });
      }
    });

    await page.addInitScript((user) => {
      localStorage.setItem('auth_user', JSON.stringify(user));
      localStorage.setItem('current_page', 'equipment');
    }, mockManager);
    await page.goto('/');

    // Select equipment type
    await page.getByRole('combobox').selectOption('Oven');

    // override GET to return the new equipment after save
    await page.route('**/api/wip_builder/equipments', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockEq]) });
      } else {
        await route.continue();
      }
    });

    await page.getByRole('button', { name: '+ New Equipment' }).click();
    await page.getByPlaceholder('Equipment Name').fill('Oven-Alpha');
    await page.getByPlaceholder('Max Capacity').fill('100');

    await page.getByRole('button', { name: 'Save Equipment' }).click();

    await expect(page.getByText('Oven-Alpha')).toBeVisible();
    await expect(page.getByText('Capacity: 100')).toBeVisible();

    // 5. Test Soft Delete
    await page.route('**/api/equipments/1', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({ status: 200 });
      }
    });

    await page.route('**/api/wip_builder/equipments', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([{ ...mockEq, currentStatus: 'OFFLINE' }]) });
      } else {
        await route.continue();
      }
    });

    const eqDeleteHandler = async (dialog: any) => { await dialog.accept(); };
    page.on('dialog', eqDeleteHandler);
    await page.getByRole('button', { name: 'Delete' }).click();
    await expect(page.getByText('(OFFLINE)')).toBeVisible();
    page.removeListener('dialog', eqDeleteHandler);

    // 6. Test Recover
    await page.route('**/api/equipments/1/recover', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({ status: 200 });
      }
    });

    await page.route('**/api/wip_builder/equipments', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockEq]) });
      } else {
        await route.continue();
      }
    });

    const eqRecoverHandler = async (dialog: any) => { await dialog.accept(); };
    page.on('dialog', eqRecoverHandler);
    await page.getByRole('button', { name: 'Recover' }).click();
    await expect(page.getByText('(OFFLINE)')).toBeHidden();
    page.removeListener('dialog', eqRecoverHandler);
  });

  test('Recipe Flow', async ({ page }) => {
    const mockSchema = {
      id: 1,
      equipmentType: 'Oven',
      parameterSchema: JSON.stringify({
        type: 'object',
        properties: { temp: { type: 'number' } },
        required: ['temp']
      })
    };

    const mockRecipe = {
      id: 1,
      name: 'Bake Pizza',
      parameters: '{"temp":250}',
      isActive: true
    };

    await page.route('**/api/equipment-schemas', route => route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockSchema]) }));

    await page.route('**/api/equipment-types/*/recipes', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: '[]' });
      } else if (route.request().method() === 'POST') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockRecipe) });
      }
    });

    await page.addInitScript((user) => {
      localStorage.setItem('auth_user', JSON.stringify(user));
      localStorage.setItem('current_page', 'recipe');
    }, mockManager);
    await page.goto('/');

    await page.getByRole('combobox').selectOption('Oven');

    await page.route('**/api/equipment-types/*/recipes', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockRecipe]) });
      } else if (route.request().method() === 'POST') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(mockRecipe) });
      } else {
        await route.continue();
      }
    });

    await page.getByRole('button', { name: '+ New Recipe' }).click();
    await page.getByPlaceholder('Recipe Name').fill('Bake Pizza');
    // RJSF dynamically generates inputs.
    await page.locator('#root_temp').fill('250');

    await page.getByRole('button', { name: 'Save Recipe' }).click();

    await expect(page.getByText('Bake Pizza')).toBeVisible();
    await expect(page.getByText('{"temp":250}')).toBeVisible();

    // 5. Test Soft Delete
    await page.route('**/api/recipes/1', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({ status: 200 });
      }
    });

    await page.route('**/api/equipment-types/*/recipes', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([{ ...mockRecipe, isActive: false }]) });
      } else {
        await route.continue();
      }
    });

    const recipeDeleteHandler = async (dialog: any) => { await dialog.accept(); };
    page.on('dialog', recipeDeleteHandler);
    await page.getByRole('button', { name: 'Delete' }).click();
    await expect(page.getByText('(INACTIVE)')).toBeVisible();
    page.removeListener('dialog', recipeDeleteHandler);

    // 6. Test Recover
    await page.route('**/api/recipes/1/recover', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({ status: 200 });
      }
    });

    await page.route('**/api/equipment-types/*/recipes', async route => {
      if (route.request().method() === 'GET') {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify([mockRecipe]) });
      } else {
        await route.continue();
      }
    });

    const recipeRecoverHandler = async (dialog: any) => { await dialog.accept(); };
    page.on('dialog', recipeRecoverHandler);
    await page.getByRole('button', { name: 'Recover' }).click();
    await expect(page.getByText('(INACTIVE)')).toBeHidden();
    page.removeListener('dialog', recipeRecoverHandler);
  });

});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import {
  fetchRecipesByEquipment,
  fetchAllRecipes,
  createRecipe,
  updateRecipe,
  deleteRecipe,
  recoverRecipe
} from '../src/features/recipe/api/recipeApi';
import { CONFIG } from '../src/config/config';

vi.mock('axios');

describe('recipeApi', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('fetchRecipesByEquipment', async () => {
    const mockData = [{ id: 1, name: 'Recipe 1' }];
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchRecipesByEquipment('Type A');
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-types/Type A/recipes`);
    expect(result).toEqual(mockData);
  });

  it('fetchAllRecipes', async () => {
    const mockData = [{ id: 1, name: 'Recipe 1' }];
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchAllRecipes();
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/recipes`);
    expect(result).toEqual(mockData);
  });

  it('createRecipe', async () => {
    const mockData = { id: 1, name: 'Recipe 1' };
    const requestData = { name: 'Recipe 1' } as any;
    vi.mocked(axios.post).mockResolvedValueOnce({ data: mockData });
    const result = await createRecipe('Type A', requestData);
    expect(axios.post).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-types/Type A/recipes`, requestData);
    expect(result).toEqual(mockData);
  });

  it('updateRecipe', async () => {
    const mockData = { id: 1, name: 'Recipe 1 Updated' };
    const requestData = { name: 'Recipe 1 Updated' } as any;
    vi.mocked(axios.put).mockResolvedValueOnce({ data: mockData });
    const result = await updateRecipe(1, requestData);
    expect(axios.put).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/recipes/1`, requestData);
    expect(result).toEqual(mockData);
  });

  it('deleteRecipe', async () => {
    vi.mocked(axios.delete).mockResolvedValueOnce({});
    await deleteRecipe(1);
    expect(axios.delete).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/recipes/1`);
  });

  it('recoverRecipe', async () => {
    vi.mocked(axios.put).mockResolvedValueOnce({});
    await recoverRecipe(1);
    expect(axios.put).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/recipes/1/recover`);
  });
});

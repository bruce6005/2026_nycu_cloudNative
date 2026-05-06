import axios from "axios";
import { CONFIG } from "../../../config/config";
import type { Recipe, RecipeRequest } from "../model/Recipe";

export async function fetchRecipesByEquipment(equipmentType: string): Promise<Recipe[]> {
    const res = await axios.get(`${CONFIG.API_BASE}/api/equipment-types/${equipmentType}/recipes`);
    return res.data;
}

export async function createRecipe(equipmentType: string, data: RecipeRequest): Promise<Recipe> {
    const res = await axios.post(`${CONFIG.API_BASE}/api/equipment-types/${equipmentType}/recipes`, data);
    return res.data;
}

export async function updateRecipe(recipeId: number, data: RecipeRequest): Promise<Recipe> {
    const res = await axios.put(`${CONFIG.API_BASE}/api/recipes/${recipeId}`, data);
    return res.data;
}

export async function deleteRecipe(recipeId: number): Promise<void> {
    await axios.delete(`${CONFIG.API_BASE}/api/recipes/${recipeId}`);
}

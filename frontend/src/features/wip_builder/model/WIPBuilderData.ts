export type PendingSampleDTO = {
  sampleId: number;
  barcode: string;
  sampleStatus: string;

  requestId: number;
  requestTitle: string;
  requestDescription: string | null;
  priority: string;

  recipeId: number | null;
  recipeName: string | null;
};

export type RecipeDTO = {
  id: number;
  name: string;
  version: string;
};

export type EquipmentWithRecipesDTO = {
  id: number;
  name: string;
  equipmentType: string;
  maxCapacity: number;
  currentStatus?: string | null;
  recipes: RecipeDTO[];
};

export type CreateWIPBatchRequest = {
  equipmentId: number;
  recipeId: number;
  sampleIds: number[];
  operatorId: number;
};
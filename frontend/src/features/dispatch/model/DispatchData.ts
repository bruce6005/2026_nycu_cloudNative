export type PendingSamplesGroupedByRequestDTO = {
  requestId: number;
  requestTitle: string;
  requestDescription: string | null;
  priority: string;
  pendingSampleCount: number;
  unassignedSampleIds: number[];
};

export type RecipeDTO = {
  id: number;
  name: string;
  version: string;
};

export type EquipmentWithRecipesDTO = {
  id: number;
  name: string;
  maxCapacity: number;
  currentStatus?: string | null;
  recipes: RecipeDTO[];
};

export type CreateWIPBatchRequest = {
  equipmentId: number;
  recipeId: number;
  sampleIds: number[];
};
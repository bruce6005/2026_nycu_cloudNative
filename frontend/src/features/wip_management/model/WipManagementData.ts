export type WIPBatchDTO = {
  id: number;
  recipeId: number;
  recipeName: string;
  equipmentId: number;
  equipmentName: string;
  sampleBarcodes: string[];
  status: string;
  createTime: string;
  startTime?: string | null;
  endTime?: string | null;

  estimatedEndTime?: string | null;
  progressPercent?: number | null;
  remainingSeconds?: number | null;
};
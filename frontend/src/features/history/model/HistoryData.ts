export type HistorySampleDTO = {
  sampleId: number;
  barcode: string;
  status: string;
  batchId?: number | null;
  batchStatus?: string | null;
  equipmentName?: string | null;
  recipeName?: string | null;
};

export type HistoryRequestGroupDTO = {
  requestId: number;
  requestTitle: string;
  requestDescription?: string | null;
  requestStatus: string;
  priority: string;
  createTime?: string | null;
  endTime?: string | null;
  sampleCount: number;
  samples: HistorySampleDTO[];
};

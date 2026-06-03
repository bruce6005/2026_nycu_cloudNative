import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import {
  fetchEquipments,
  createEquipment,
  deleteEquipment,
  recoverEquipment,
  fetchEquipmentsWithStatus,
  fetchEquipmentSchemas,
  createEquipmentSchema,
  fetchEquipmentSchemaByType,
  deleteEquipmentSchema
} from '../src/features/equipment/api/equipmentApi';
import { CONFIG } from '../src/config/config';

vi.mock('axios');

describe('equipmentApi', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('fetchEquipments', async () => {
    const mockData = [{ id: 1, name: 'Eq 1' }];
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchEquipments();
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipments`);
    expect(result).toEqual(mockData);
  });

  it('createEquipment', async () => {
    const mockData = { id: 1, name: 'Eq 1' };
    const requestData = { name: 'Eq 1', type: 'Type A' } as any;
    vi.mocked(axios.post).mockResolvedValueOnce({ data: mockData });
    const result = await createEquipment(requestData);
    expect(axios.post).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipments`, requestData);
    expect(result).toEqual(mockData);
  });

  it('deleteEquipment', async () => {
    vi.mocked(axios.delete).mockResolvedValueOnce({});
    await deleteEquipment(1);
    expect(axios.delete).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipments/1`);
  });

  it('recoverEquipment', async () => {
    vi.mocked(axios.put).mockResolvedValueOnce({});
    await recoverEquipment(1);
    expect(axios.put).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipments/1/recover`);
  });

  it('fetchEquipmentsWithStatus', async () => {
    const mockData = [{ id: 1, recipes: [] }];
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchEquipmentsWithStatus();
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/wip_builder/equipments`);
    expect(result).toEqual(mockData);
  });

  it('fetchEquipmentSchemas', async () => {
    const mockData = [{ id: 1, type: 'Type A' }];
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchEquipmentSchemas();
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-schemas`);
    expect(result).toEqual(mockData);
  });

  it('createEquipmentSchema', async () => {
    const mockData = { id: 1, equipmentType: 'Type A', parameterSchema: '{}' };
    const requestData = { equipmentType: 'Type A', parameterSchema: '{}' };
    vi.mocked(axios.post).mockResolvedValueOnce({ data: mockData });
    const result = await createEquipmentSchema(requestData);
    expect(axios.post).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-schemas`, requestData);
    expect(result).toEqual(mockData);
  });

  it('fetchEquipmentSchemaByType', async () => {
    const mockData = { id: 1, type: 'Type A' };
    vi.mocked(axios.get).mockResolvedValueOnce({ data: mockData });
    const result = await fetchEquipmentSchemaByType('Type A');
    expect(axios.get).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-schemas/Type A`);
    expect(result).toEqual(mockData);
  });

  it('deleteEquipmentSchema', async () => {
    vi.mocked(axios.delete).mockResolvedValueOnce({});
    await deleteEquipmentSchema(1);
    expect(axios.delete).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/equipment-schemas/1`);
  });
});

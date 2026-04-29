import axios from "axios";
import { CONFIG } from "../../../config/config";
import type { Equipment, EquipmentRequest, EquipmentTypeSchema } from "../model/Equipment";

export async function fetchEquipments(): Promise<Equipment[]> {
    const res = await axios.get(`${CONFIG.API_BASE}/api/equipments`);
    return res.data;
}

export async function createEquipment(data: EquipmentRequest): Promise<Equipment> {
    const res = await axios.post(`${CONFIG.API_BASE}/api/equipments`, data);
    return res.data;
}

export async function fetchEquipmentSchemas(): Promise<EquipmentTypeSchema[]> {
    const res = await axios.get(`${CONFIG.API_BASE}/api/equipment-schemas`);
    return res.data;
}

export async function fetchEquipmentSchemaByType(type: string): Promise<EquipmentTypeSchema> {
    const res = await axios.get(`${CONFIG.API_BASE}/api/equipment-schemas/${type}`);
    return res.data;
}

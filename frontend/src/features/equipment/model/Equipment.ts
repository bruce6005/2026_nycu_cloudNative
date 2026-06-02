import type { AuthUser } from "../../auth/model/AuthUser";

export interface EquipmentTypeSchema {
    id: number;
    equipmentType: string;
    parameterSchema: string;
}

export interface Equipment {
    id: number;
    name: string;
    type: string;
    equipmentTypeSchemaId: number;
    maxCapacity: number;
    handler: AuthUser | null;
}

export interface EquipmentRequest {
    name: string;
    type?: string;
    equipmentTypeSchemaId?: number;
    maxCapacity: number;
    handlerId?: number | null;
}

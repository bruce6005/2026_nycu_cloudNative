export interface Recipe {
    id: number;
    equipmentType: string;
    name: string;
    parameters: string;
    isActive: boolean;
}

export interface RecipeRequest {
    name: string;
    parameters: string;
    isActive?: boolean;
}

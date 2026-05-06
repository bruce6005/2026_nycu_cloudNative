import { useState, useEffect } from "react";
import { fetchEquipmentSchemas } from "../../equipment/api/equipmentApi";
import type { EquipmentTypeSchema } from "../../equipment/model/Equipment";
import type { Recipe, RecipeRequest } from "../model/Recipe";
import { fetchRecipesByEquipment, createRecipe, deleteRecipe } from "../api/recipeApi";
import Form from "@rjsf/core";
import validator from "@rjsf/validator-ajv8";
import "../../approval/styles/style.css";

export default function RecipeManagementPage() {
    const [schemas, setSchemas] = useState<EquipmentTypeSchema[]>([]);
    const [selectedEquipmentType, setSelectedEquipmentType] = useState<string>("");
    const [recipes, setRecipes] = useState<Recipe[]>([]);
    const [parsedSchema, setParsedSchema] = useState<any>(null);
    const [showForm, setShowForm] = useState(false);
    const [newRecipe, setNewRecipe] = useState<{name: string}>({ name: "" });
    const [formData, setFormData] = useState<any>({});

    useEffect(() => {
        fetchEquipmentSchemas().then(setSchemas).catch(console.error);
    }, []);

    useEffect(() => {
        if (selectedEquipmentType) {
            loadData(selectedEquipmentType);
        } else {
            setRecipes([]);
            setParsedSchema(null);
            setShowForm(false);
        }
    }, [selectedEquipmentType]);

    const loadData = async (type: string) => {
        try {
            const res = await fetchRecipesByEquipment(type);
            setRecipes(res);
            const schemaObj = schemas.find(s => s.equipmentType === type);
            if (schemaObj && schemaObj.parameterSchema) {
                setParsedSchema(JSON.parse(schemaObj.parameterSchema));
            }
        } catch (error) {
            console.error("Failed to load recipes or schema", error);
        }
    };

    const handleSubmit = async () => {
        if (!newRecipe.name || !selectedEquipmentType) return;
        try {
            const request: RecipeRequest = {
                name: newRecipe.name,
                parameters: JSON.stringify(formData),
                isActive: true
            };
            await createRecipe(selectedEquipmentType, request);
            setShowForm(false);
            setNewRecipe({ name: "" });
            setFormData({});
            loadData(selectedEquipmentType);
        } catch (error: any) {
            console.error("Failed to create recipe", error);
            alert(error.response?.data?.message || "Failed to create recipe. The name might already exist.");
        }
    };

    const handleDelete = async (id: number) => {
        if (window.confirm("Are you sure you want to delete this recipe?")) {
            try {
                await deleteRecipe(id);
                loadData(selectedEquipmentType);
            } catch (error) {
                console.error("Failed to delete recipe", error);
            }
        }
    };

    const renderSchemaConstraints = () => {
        if (!parsedSchema || !parsedSchema.properties) return null;
        return (
            <div style={{ background: "#fdfdfd", padding: "15px", borderRadius: "8px", border: "1px solid #ddd", marginBottom: "15px", fontSize: "14px" }}>
                <h4 style={{ margin: "0 0 10px 0", color: "#333" }}>Parameter Constraints</h4>
                <ul style={{ margin: 0, paddingLeft: "20px", color: "#555" }}>
                    {Object.entries(parsedSchema.properties).map(([key, value]: [string, any]) => (
                        <li key={key} style={{ marginBottom: "5px" }}>
                            <strong>{key}</strong> ({value.type}): 
                            {value.enum && ` Allowed values: [${value.enum.join(", ")}] `}
                            {value.minimum !== undefined && ` Min: ${value.minimum} `}
                            {value.maximum !== undefined && ` Max: ${value.maximum} `}
                            {parsedSchema.required?.includes(key) && <span style={{ color: "#d9534f", fontSize: "12px", marginLeft: "4px" }}>*Required</span>}
                        </li>
                    ))}
                </ul>
            </div>
        );
    };

    return (
        <div style={{ padding: "20px" }}>
            <h1 style={{ marginBottom: "20px" }}>Recipe Management</h1>
            <div className="card" style={{ marginBottom: "20px" }}>
                <label style={{ marginRight: "10px", fontWeight: "bold" }}>Select Equipment Type:</label>
                <select 
                    value={selectedEquipmentType} 
                    onChange={e => setSelectedEquipmentType(e.target.value)}
                    style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc", width: "300px" }}
                >
                    <option value="">-- Choose Equipment Type --</option>
                    {schemas.map(s => (
                        <option key={s.equipmentType} value={s.equipmentType}>{s.equipmentType}</option>
                    ))}
                </select>
            </div>

            {selectedEquipmentType && (
                <>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px" }}>
                        <h2 style={{ margin: 0 }}>Recipes for {selectedEquipmentType}</h2>
                        {!showForm && (
                            <button 
                                onClick={() => setShowForm(true)}
                                className="login-btn"
                                style={{ background: "#2c2c2c", color: "white" }}
                            >
                                + New Recipe
                            </button>
                        )}
                    </div>

                    {showForm && (
                        <div className="card" style={{ marginBottom: "20px" }}>
                            <h3>Create New Recipe</h3>
                            <div className="form-row">
                                <input 
                                    type="text" 
                                    placeholder="Recipe Name"
                                    value={newRecipe.name} 
                                    onChange={e => setNewRecipe({...newRecipe, name: e.target.value})} 
                                    style={{ flex: "0 0 300px" }}
                                />
                            </div>
                            <hr style={{ margin: "20px 0", borderTop: "1px solid #eee" }} />
                            {parsedSchema ? (
                                <div>
                                    {renderSchemaConstraints()}
                                    <div style={{ marginTop: "15px" }}>
                                        <Form 
                                            schema={parsedSchema} 
                                            validator={validator}
                                            formData={formData}
                                            onChange={e => setFormData(e.formData)}
                                            onSubmit={handleSubmit}
                                        >
                                            <div style={{ marginTop: "15px", display: "flex", gap: "10px" }}>
                                                <button type="submit" className="login-btn" style={{ background: "#2c2c2c", color: "white" }}>Save Recipe</button>
                                                <button type="button" className="login-btn" style={{ background: "#eee" }} onClick={() => setShowForm(false)}>Cancel</button>
                                            </div>
                                        </Form>
                                    </div>
                                </div>
                            ) : (
                                <div style={{ padding: "15px", background: "#fff3cd", color: "#856404", borderRadius: "4px" }}>
                                    No schema found for this equipment type.
                                    <br/><br/>
                                    <button className="login-btn" onClick={() => setShowForm(false)}>Cancel</button>
                                </div>
                            )}
                        </div>
                    )}

                    <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                        {recipes.map(recipe => (
                            <div key={recipe.id} className="order-card">
                                <div className="order-card-header">
                                    <div className="order-title">{recipe.name}</div>
                                    <button 
                                        onClick={(e) => { e.stopPropagation(); handleDelete(recipe.id); }}
                                        style={{ background: "transparent", color: "#e5484d", border: "none", cursor: "pointer", fontSize: "14px", fontWeight: "bold" }}
                                    >
                                        Delete
                                    </button>
                                </div>
                                <div className="order-sub" style={{ marginTop: "8px", fontFamily: "monospace" }}>
                                    {recipe.parameters}
                                </div>
                            </div>
                        ))}
                        {recipes.length === 0 && (
                            <div className="card">No recipes found.</div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}

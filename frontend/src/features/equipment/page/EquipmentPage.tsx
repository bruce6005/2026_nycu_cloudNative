import { useState, useEffect } from "react";
import type { Equipment, EquipmentTypeSchema } from "../model/Equipment";
import { createEquipment, deleteEquipment, fetchEquipments, fetchEquipmentSchemas } from "../api/equipmentApi";
import "../../approval/styles/style.css";

export default function EquipmentPage() {
    const [equipments, setEquipments] = useState<Equipment[]>([]);
    const [schemas, setSchemas] = useState<EquipmentTypeSchema[]>([]);
    const [showForm, setShowForm] = useState(false);
    const [formData, setFormData] = useState({
        name: "",
        equipmentTypeSchemaId: "",
        maxCapacity: "",
    });

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            const [equipmentList, schemaList] = await Promise.all([
                fetchEquipments(),
                fetchEquipmentSchemas(),
            ]);
            setEquipments(equipmentList);
            setSchemas(schemaList);
        } catch (error) {
            console.error("Failed to load equipment data", error);
        }
    };

    const resetForm = () => {
        setFormData({
            name: "",
            equipmentTypeSchemaId: "",
            maxCapacity: "",
        });
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const maxCapacity = Number(formData.maxCapacity);
        const equipmentTypeSchemaId = Number(formData.equipmentTypeSchemaId);

        if (!formData.name.trim() || !equipmentTypeSchemaId || !Number.isFinite(maxCapacity) || maxCapacity <= 0) {
            alert("Please fill in a valid equipment name, type, and capacity.");
            return;
        }

        try {
            await createEquipment({
                name: formData.name.trim(),
                equipmentTypeSchemaId,
                maxCapacity,
                handlerId: null,
            });
            resetForm();
            setShowForm(false);
            await loadData();
        } catch (error: any) {
            console.error("Failed to create equipment", error);
            alert(error.response?.data?.message || "Failed to create equipment.");
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this equipment?")) return;

        try {
            await deleteEquipment(id);
            await loadData();
        } catch (error: any) {
            console.error("Failed to delete equipment", error);
            alert(error.response?.data?.message || "Failed to delete equipment.");
        }
    };

    return (
        <div style={{ padding: "20px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
                <h1 style={{ margin: 0 }}>Equipment List</h1>
                {!showForm && (
                    <button
                        className="login-btn"
                        style={{ background: "#2c2c2c", color: "white" }}
                        onClick={() => setShowForm(true)}
                    >
                        + New Equipment
                    </button>
                )}
            </div>

            {showForm && (
                <div className="card" style={{ marginBottom: "20px" }}>
                    <h3 style={{ marginTop: 0 }}>Create New Equipment</h3>
                    <form onSubmit={handleSubmit}>
                        <div className="form-row" style={{ display: "flex", gap: "10px", flexWrap: "wrap", alignItems: "center" }}>
                            <input
                                type="text"
                                placeholder="Equipment Name"
                                value={formData.name}
                                onChange={(event) => setFormData({ ...formData, name: event.target.value })}
                                style={{ flex: "1 1 260px" }}
                            />
                            <select
                                value={formData.equipmentTypeSchemaId}
                                onChange={(event) => setFormData({ ...formData, equipmentTypeSchemaId: event.target.value })}
                                style={{ flex: "1 1 260px", padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
                            >
                                <option value="">Select Equipment Type</option>
                                {schemas.map((schema) => (
                                    <option key={schema.id} value={schema.id}>
                                        {schema.equipmentType}
                                    </option>
                                ))}
                            </select>
                            <input
                                type="number"
                                min="1"
                                placeholder="Max Capacity"
                                value={formData.maxCapacity}
                                onChange={(event) => setFormData({ ...formData, maxCapacity: event.target.value })}
                                style={{ flex: "0 1 180px" }}
                            />
                        </div>
                        <div style={{ marginTop: "15px", display: "flex", gap: "10px" }}>
                            <button type="submit" className="login-btn" style={{ background: "#2c2c2c", color: "white" }}>
                                Save Equipment
                            </button>
                            <button
                                type="button"
                                className="login-btn"
                                style={{ background: "#eee" }}
                                onClick={() => {
                                    resetForm();
                                    setShowForm(false);
                                }}
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                {equipments.map((eq) => (
                    <div key={eq.id} className="order-card">
                        <div className="order-card-header">
                            <div className="order-title">{eq.name}</div>
                            <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                <div className="order-tag">{eq.type}</div>
                                <button
                                    type="button"
                                    onClick={(event) => {
                                        event.stopPropagation();
                                        handleDelete(eq.id);
                                    }}
                                    style={{ background: "transparent", color: "#e5484d", border: "none", cursor: "pointer", fontSize: "14px", fontWeight: "bold" }}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                        <div className="order-sub">Capacity: {eq.maxCapacity}</div>
                    </div>
                ))}
                {equipments.length === 0 && (
                    <div className="card">No equipment available.</div>
                )}
            </div>
        </div>
    );
}

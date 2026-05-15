import { useState, useEffect } from "react";
import type { EquipmentTypeSchema } from "../model/Equipment";
import type { EquipmentWithRecipesDTO } from "../../wip_builder/model/WIPBuilderData";
import { createEquipment, deleteEquipment, fetchEquipmentsWithStatus, fetchEquipmentSchemas, recoverEquipment } from "../api/equipmentApi";
import "../../approval/styles/style.css";

export default function EquipmentPage() {
    const [equipments, setEquipments] = useState<EquipmentWithRecipesDTO[]>([]);
    const [schemas, setSchemas] = useState<EquipmentTypeSchema[]>([]);
    const [selectedEquipmentType, setSelectedEquipmentType] = useState<string>("");
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
                fetchEquipmentsWithStatus(),
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
        if (!window.confirm("Are you sure you want to soft delete this equipment?")) return;

        try {
            await deleteEquipment(id);
            await loadData();
        } catch (error: any) {
            console.error("Failed to delete equipment", error);
            alert(error.response?.data?.message || "Failed to delete equipment.");
        }
    };

    const handleRecover = async (id: number) => {
        if (!window.confirm("Are you sure you want to recover this equipment?")) return;

        try {
            await recoverEquipment(id);
            await loadData();
        } catch (error: any) {
            console.error("Failed to recover equipment", error);
            alert(error.response?.data?.message || "Failed to recover equipment.");
        }
    };

    const filteredEquipments = selectedEquipmentType
        ? equipments.filter((eq) => eq.equipmentType === selectedEquipmentType)
        : [];

    return (
        <div style={{ padding: "20px" }}>
            <h1 style={{ marginBottom: "20px" }}>Equipment Management</h1>
            <div className="card" style={{ marginBottom: "20px" }}>
                <label style={{ marginRight: "10px", fontWeight: "bold" }}>Select Equipment Type:</label>
                <select
                    value={selectedEquipmentType}
                    onChange={(e) => setSelectedEquipmentType(e.target.value)}
                    style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc", width: "300px" }}
                >
                    <option value="">-- Choose Equipment Type --</option>
                    {schemas.map((s) => (
                        <option key={s.equipmentType} value={s.equipmentType}>
                            {s.equipmentType}
                        </option>
                    ))}
                </select>
            </div>

            {selectedEquipmentType && (
                <>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "15px" }}>
                        <h2 style={{ margin: 0 }}>Equipments for {selectedEquipmentType}</h2>
                        {!showForm && (
                            <button
                                className="login-btn"
                                style={{ background: "#2c2c2c", color: "white" }}
                                onClick={() => {
                                    const schema = schemas.find(s => s.equipmentType === selectedEquipmentType);
                                    if (schema) {
                                        setFormData(prev => ({ ...prev, equipmentTypeSchemaId: String(schema.id) }));
                                    }
                                    setShowForm(true);
                                }}
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
                                        disabled
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
                        {filteredEquipments.map((eq) => (
                            <div key={eq.id} className="order-card" style={{ opacity: eq.currentStatus === 'OFFLINE' ? 0.6 : 1 }}>
                                <div className="order-card-header">
                                    <div className="order-title">
                                        {eq.name}
                                        {eq.currentStatus === 'OFFLINE' && <span style={{ marginLeft: '10px', fontSize: '12px', color: '#e5484d', fontWeight: 'bold' }}>(OFFLINE)</span>}
                                    </div>
                                    <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                        <div className="order-tag">{eq.equipmentType}</div>
                                        {eq.currentStatus === 'OFFLINE' ? (
                                            <button
                                                type="button"
                                                onClick={(event) => {
                                                    event.stopPropagation();
                                                    handleRecover(eq.id);
                                                }}
                                                style={{ background: "transparent", color: "#4caf50", border: "none", cursor: "pointer", fontSize: "14px", fontWeight: "bold" }}
                                            >
                                                Recover
                                            </button>
                                        ) : (
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
                                        )}
                                    </div>
                                </div>
                                <div className="order-sub">Capacity: {eq.maxCapacity} | Current Status: {eq.currentStatus || 'UNKNOWN'}</div>
                            </div>
                        ))}
                        {filteredEquipments.length === 0 && (
                            <div className="card">No equipment available for this type.</div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}

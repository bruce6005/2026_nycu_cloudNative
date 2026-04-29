import { useEffect, useMemo, useState } from "react";
import {
    createEquipmentSchema,
    deleteEquipmentSchema,
    fetchEquipmentSchemas,
} from "../api/equipmentApi";
import type { EquipmentTypeSchema } from "../model/Equipment";
import "../../approval/styles/style.css";

type ParameterType = "string" | "number" | "integer" | "boolean";

type ParameterRow = {
    id: number;
    name: string;
    type: ParameterType;
    required: boolean;
    minimum: string;
    maximum: string;
    enumValues: string;
};

const createEmptyRow = (): ParameterRow => ({
    id: Date.now() + Math.random(),
    name: "",
    type: "string",
    required: false,
    minimum: "",
    maximum: "",
    enumValues: "",
});

const parseSchemaRows = (parameterSchema: string): ParameterRow[] => {
    try {
        const schema = JSON.parse(parameterSchema);
        const properties = schema.properties ?? {};
        const required = Array.isArray(schema.required) ? schema.required : [];

        return Object.entries(properties).map(([name, value]: [string, any]) => ({
            id: Date.now() + Math.random(),
            name,
            type: ["string", "number", "integer", "boolean"].includes(value.type) ? value.type : "string",
            required: required.includes(name),
            minimum: value.minimum !== undefined ? String(value.minimum) : "",
            maximum: value.maximum !== undefined ? String(value.maximum) : "",
            enumValues: Array.isArray(value.enum) ? value.enum.join(", ") : "",
        }));
    } catch {
        return [];
    }
};

const buildParameterSchema = (rows: ParameterRow[]) => {
    const properties: Record<string, any> = {};
    const required: string[] = [];

    rows.forEach((row) => {
        const name = row.name.trim();
        if (!name) return;

        const property: Record<string, any> = { type: row.type };
        const enumValues = row.enumValues
            .split(",")
            .map((value) => value.trim())
            .filter(Boolean);

        if (enumValues.length > 0) {
            property.enum = row.type === "number" || row.type === "integer"
                ? enumValues.map(Number)
                : enumValues;
        }

        if ((row.type === "number" || row.type === "integer") && row.minimum.trim()) {
            property.minimum = Number(row.minimum);
        }

        if ((row.type === "number" || row.type === "integer") && row.maximum.trim()) {
            property.maximum = Number(row.maximum);
        }

        properties[name] = property;

        if (row.required) {
            required.push(name);
        }
    });

    return {
        type: "object",
        properties,
        required,
    };
};

export default function EquipmentTypeManagementPage() {
    const [schemas, setSchemas] = useState<EquipmentTypeSchema[]>([]);
    const [showForm, setShowForm] = useState(false);
    const [equipmentType, setEquipmentType] = useState("");
    const [parameterRows, setParameterRows] = useState<ParameterRow[]>([createEmptyRow()]);

    const schemaPreview = useMemo(() => {
        return buildParameterSchema(parameterRows);
    }, [parameterRows]);

    useEffect(() => {
        loadSchemas();
    }, []);

    const loadSchemas = async () => {
        try {
            const res = await fetchEquipmentSchemas();
            setSchemas(res);
        } catch (error) {
            console.error("Failed to load equipment types", error);
        }
    };

    const resetForm = () => {
        setEquipmentType("");
        setParameterRows([createEmptyRow()]);
    };

    const updateRow = (id: number, patch: Partial<ParameterRow>) => {
        setParameterRows((currentRows) =>
            currentRows.map((row) => (row.id === id ? { ...row, ...patch } : row))
        );
    };

    const removeRow = (id: number) => {
        setParameterRows((currentRows) => {
            if (currentRows.length === 1) return currentRows;
            return currentRows.filter((row) => row.id !== id);
        });
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const trimmedType = equipmentType.trim();
        const names = parameterRows.map((row) => row.name.trim()).filter(Boolean);
        const uniqueNames = new Set(names);

        if (!trimmedType) {
            alert("Please enter an equipment type name.");
            return;
        }

        if (names.length === 0) {
            alert("Please add at least one parameter.");
            return;
        }

        if (uniqueNames.size !== names.length) {
            alert("Parameter names must be unique.");
            return;
        }

        if (parameterRows.some((row) => (row.type === "number" || row.type === "integer") && ((row.minimum && Number.isNaN(Number(row.minimum))) || (row.maximum && Number.isNaN(Number(row.maximum)))))) {
            alert("Numeric minimum and maximum values must be valid numbers.");
            return;
        }

        if (parameterRows.some((row) => {
            if (row.type !== "number" && row.type !== "integer") return false;
            return row.enumValues
                .split(",")
                .map((value) => value.trim())
                .filter(Boolean)
                .some((value) => Number.isNaN(Number(value)));
        })) {
            alert("Allowed values for numeric parameters must be valid numbers.");
            return;
        }

        try {
            await createEquipmentSchema({
                equipmentType: trimmedType,
                parameterSchema: JSON.stringify(schemaPreview),
            });
            resetForm();
            setShowForm(false);
            await loadSchemas();
        } catch (error: any) {
            console.error("Failed to create equipment type", error);
            alert(error.response?.data?.message || "Failed to create equipment type.");
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this equipment type?")) return;

        try {
            await deleteEquipmentSchema(id);
            await loadSchemas();
        } catch (error: any) {
            console.error("Failed to delete equipment type", error);
            alert(error.response?.data?.message || "Failed to delete equipment type. It may still be used by equipment or recipes.");
        }
    };

    const renderParameterSummary = (schema: EquipmentTypeSchema) => {
        const rows = parseSchemaRows(schema.parameterSchema);
        if (rows.length === 0) {
            return <div className="order-sub">No parameters defined.</div>;
        }

        return (
            <div style={{ marginTop: "10px", display: "grid", gap: "8px" }}>
                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "minmax(120px, 1fr) 100px minmax(180px, 2fr)",
                        gap: "10px",
                        alignItems: "center",
                        fontSize: "12px",
                        fontWeight: 600,
                        color: "#555",
                        borderBottom: "1px solid #ddd",
                        paddingBottom: "6px",
                    }}
                >
                    <span>Name</span>
                    <span>Type</span>
                    <span>Constraints</span>
                </div>
                {rows.map((row) => (
                    <div
                        key={`${schema.id}-${row.name}`}
                        style={{
                            display: "grid",
                            gridTemplateColumns: "minmax(120px, 1fr) 100px minmax(180px, 2fr)",
                            gap: "10px",
                            alignItems: "center",
                            fontSize: "13px",
                        }}
                    >
                        <strong>{row.name}</strong>
                        <span>{row.type}{row.required ? " *" : ""}</span>
                        <span style={{ color: "#555" }}>
                            {[
                                row.enumValues ? `Allowed: ${row.enumValues}` : "",
                                row.minimum ? `Min: ${row.minimum}` : "",
                                row.maximum ? `Max: ${row.maximum}` : "",
                            ].filter(Boolean).join(" | ") || "No constraints"}
                        </span>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div style={{ padding: "20px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
                <h1 style={{ margin: 0 }}>Equipment Type Management</h1>
                {!showForm && (
                    <button
                        className="login-btn"
                        style={{ background: "#2c2c2c", color: "white" }}
                        onClick={() => setShowForm(true)}
                    >
                        + New Type
                    </button>
                )}
            </div>

            {showForm && (
                <div className="card" style={{ marginBottom: "20px" }}>
                    <h3 style={{ marginTop: 0 }}>Create Equipment Type</h3>
                    <form onSubmit={handleSubmit}>
                        <div className="form-row">
                            <input
                                type="text"
                                placeholder="Equipment Type Name"
                                value={equipmentType}
                                onChange={(event) => setEquipmentType(event.target.value)}
                                style={{ width: "320px" }}
                            />
                        </div>

                        <div style={{ marginTop: "14px", display: "grid", gap: "10px" }}>
                            {parameterRows.map((row) => (
                                <div
                                    key={row.id}
                                    style={{
                                        display: "grid",
                                        gridTemplateColumns: "minmax(160px, 1.3fr) 120px 90px 110px 110px minmax(160px, 1.4fr) 80px",
                                        gap: "8px",
                                        alignItems: "center",
                                    }}
                                >
                                    <input
                                        type="text"
                                        placeholder="Parameter Name"
                                        value={row.name}
                                        onChange={(event) => updateRow(row.id, { name: event.target.value })}
                                    />
                                    <select
                                        value={row.type}
                                        onChange={(event) => updateRow(row.id, { type: event.target.value as ParameterType })}
                                        style={{ padding: "8px", borderRadius: "4px", border: "1px solid #ccc" }}
                                    >
                                        <option value="string">String</option>
                                        <option value="number">Number</option>
                                        <option value="integer">Integer</option>
                                        <option value="boolean">Boolean</option>
                                    </select>
                                    <label style={{ display: "flex", alignItems: "center", gap: "6px", fontSize: "13px" }}>
                                        <input
                                            type="checkbox"
                                            checked={row.required}
                                            onChange={(event) => updateRow(row.id, { required: event.target.checked })}
                                        />
                                        Required
                                    </label>
                                    <input
                                        type="number"
                                        placeholder="Min"
                                        value={row.minimum}
                                        disabled={row.type !== "number" && row.type !== "integer"}
                                        onChange={(event) => updateRow(row.id, { minimum: event.target.value })}
                                    />
                                    <input
                                        type="number"
                                        placeholder="Max"
                                        value={row.maximum}
                                        disabled={row.type !== "number" && row.type !== "integer"}
                                        onChange={(event) => updateRow(row.id, { maximum: event.target.value })}
                                    />
                                    <input
                                        type="text"
                                        placeholder="Allowed values"
                                        value={row.enumValues}
                                        disabled={row.type === "boolean"}
                                        onChange={(event) => updateRow(row.id, { enumValues: event.target.value })}
                                    />
                                    <button
                                        type="button"
                                        className="login-btn"
                                        style={{ background: "#eee" }}
                                        onClick={() => removeRow(row.id)}
                                    >
                                        Remove
                                    </button>
                                </div>
                            ))}
                        </div>

                        <div style={{ marginTop: "12px" }}>
                            <button
                                type="button"
                                className="login-btn"
                                style={{ background: "#eee" }}
                                onClick={() => setParameterRows([...parameterRows, createEmptyRow()])}
                            >
                                + Add Parameter
                            </button>
                        </div>

                        <div style={{ marginTop: "15px", display: "flex", gap: "10px" }}>
                            <button type="submit" className="login-btn" style={{ background: "#2c2c2c", color: "white" }}>
                                Save Type
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
                {schemas.map((schema) => (
                    <div key={schema.id} className="order-card">
                        <div className="order-card-header">
                            <div>
                                <div className="order-title">{schema.equipmentType}</div>
                                <div className="order-sub">ID: {schema.id}</div>
                            </div>
                            <button
                                type="button"
                                onClick={(event) => {
                                    event.stopPropagation();
                                    handleDelete(schema.id);
                                }}
                                style={{ background: "transparent", color: "#e5484d", border: "none", cursor: "pointer", fontSize: "14px", fontWeight: "bold" }}
                            >
                                Delete
                            </button>
                        </div>
                        {renderParameterSummary(schema)}
                    </div>
                ))}
                {schemas.length === 0 && (
                    <div className="card">No equipment types available.</div>
                )}
            </div>
        </div>
    );
}

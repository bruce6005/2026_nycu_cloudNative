import { useState, useEffect } from "react";
import type { Equipment } from "../model/Equipment";
import { fetchEquipments } from "../api/equipmentApi";
import "../../approval/styles/style.css";

export default function EquipmentPage() {
    const [equipments, setEquipments] = useState<Equipment[]>([]);

    useEffect(() => {
        fetchEquipments().then(setEquipments).catch(console.error);
    }, []);

    return (
        <div style={{ padding: "20px" }}>
            <h1 style={{ marginBottom: "20px" }}>Equipment List</h1>
            <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                {equipments.map((eq) => (
                    <div key={eq.id} className="order-card">
                        <div className="order-card-header">
                            <div className="order-title">{eq.name}</div>
                            <div className="order-tag">{eq.type}</div>
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

package com.example.demo.modules.recipe.model;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipe", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"equipment_type_schema_id", "name"})
})
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_schema_id", nullable = false)
    private EquipmentTypeSchema equipmentTypeSchema;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "json", nullable = false)
    private String parameters;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Transient
    public String getEquipmentType() {
        return equipmentTypeSchema != null ? equipmentTypeSchema.getEquipmentType() : null;
    }

    @Transient
    public Long getEquipmentTypeSchemaId() {
        return equipmentTypeSchema != null ? equipmentTypeSchema.getId() : null;
    }
}

package com.example.demo.modules.equipment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.demo.modules.auth.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_schema_id", nullable = false)
    private EquipmentTypeSchema equipmentTypeSchema;

    @Column(nullable = false)
    private String name;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @Transient
    public String getType() {
        return equipmentTypeSchema != null ? equipmentTypeSchema.getEquipmentType() : null;
    }

    @Transient
    public Long getEquipmentTypeSchemaId() {
        return equipmentTypeSchema != null ? equipmentTypeSchema.getId() : null;
    }
}

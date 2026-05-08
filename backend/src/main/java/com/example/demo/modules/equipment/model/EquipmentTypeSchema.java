package com.example.demo.modules.equipment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "equipment_type_schema")
public class EquipmentTypeSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_type", unique = true, nullable = false)
    private String equipmentType;

    @Column(name = "parameter_schema", columnDefinition = "json", nullable = false)
    private String parameterSchema;
}

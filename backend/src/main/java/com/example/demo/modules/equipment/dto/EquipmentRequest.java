package com.example.demo.modules.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentRequest {
    private String name;
    private String type;
    private Long equipmentTypeSchemaId;
    private Integer maxCapacity;
    private Long handlerId;
}

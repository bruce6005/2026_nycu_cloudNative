package com.example.demo.modules.equipment.dto;

import lombok.Data;

@Data
public class EquipmentRequest {
    private String name;
    private String type;
    private Integer maxCapacity;
    private Long handlerId;
}

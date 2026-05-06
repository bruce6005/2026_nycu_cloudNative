package com.example.demo.modules.equipment.dto;

import lombok.Data;

@Data
public class ResolveAlarmRequest {

    private Long handlerId;
    private String notes;
}
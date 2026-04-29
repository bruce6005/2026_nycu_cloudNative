package com.example.demo.modules.dispatch.dto;

import java.util.List;

import lombok.Data;

@Data
public class EquipmentWithRecipesDTO {
    private Long id;
    private String name;
    private Integer maxCapacity;
    private String currentStatus;
    private List<RecipeDTO> recipes;
}

package com.example.demo.modules.recipe.dto;

import lombok.Data;

@Data
public class RecipeRequest {
    private String name;
    private String parameters;
    private Boolean isActive;
}

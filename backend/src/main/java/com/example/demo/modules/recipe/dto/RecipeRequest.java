package com.example.demo.modules.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecipeRequest {
    private String name;
    private String parameters;
    private Boolean isActive;
}

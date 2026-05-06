package com.example.demo.modules.recipe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.modules.recipe.dto.RecipeRequest;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.recipe.service.RecipeService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/equipment-types/{equipmentType}/recipes")
    public ResponseEntity<List<Recipe>> getRecipesByEquipmentType(@PathVariable String equipmentType) {
        return ResponseEntity.ok(recipeService.getRecipesByEquipmentType(equipmentType));
    }

    @GetMapping("/recipes/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @PostMapping("/equipment-types/{equipmentType}/recipes")
    public ResponseEntity<Recipe> createRecipe(@PathVariable String equipmentType, @RequestBody RecipeRequest request) {
        return ResponseEntity.ok(recipeService.createRecipe(equipmentType, request));
    }

    @PutMapping("/recipes/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable Long id, @RequestBody RecipeRequest request) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request));
    }

    @DeleteMapping("/recipes/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}

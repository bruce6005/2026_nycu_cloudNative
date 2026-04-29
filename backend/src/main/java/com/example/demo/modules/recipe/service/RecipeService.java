package com.example.demo.modules.recipe.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;
import com.example.demo.modules.recipe.dto.RecipeRequest;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.recipe.repository.RecipeRepository;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final EquipmentTypeSchemaRepository schemaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeService(RecipeRepository recipeRepository, EquipmentTypeSchemaRepository schemaRepository) {
        this.recipeRepository = recipeRepository;
        this.schemaRepository = schemaRepository;
    }

    public List<Recipe> getRecipesByEquipmentType(String equipmentType) {
        EquipmentTypeSchema schema = getSchemaByEquipmentType(equipmentType);
        return recipeRepository.findByEquipmentTypeSchema_Id(schema.getId());
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    @Transactional
    public Recipe createRecipe(String equipmentType, RecipeRequest request) {
        EquipmentTypeSchema schema = getSchemaByEquipmentType(equipmentType);

        if (recipeRepository.existsByEquipmentTypeSchema_IdAndName(schema.getId(), request.getName())) {
            throw new RuntimeException("Recipe name already exists for this equipment type");
        }

        List<Recipe> existingRecipes = recipeRepository.findByEquipmentTypeSchema_Id(schema.getId());
        try {
            JsonNode newParams = objectMapper.readTree(request.getParameters());
            for (Recipe existing : existingRecipes) {
                JsonNode existingParams = objectMapper.readTree(existing.getParameters());
                if (existingParams.equals(newParams)) {
                    throw new RuntimeException("A recipe with identical parameters already exists: " + existing.getName());
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException("Error parsing parameters for comparison", e);
        }

        Recipe recipe = new Recipe();
        recipe.setEquipmentTypeSchema(schema);
        recipe.setName(request.getName());
        recipe.setParameters(request.getParameters());
        recipe.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateRecipe(Long id, RecipeRequest request) {
        Recipe recipe = getRecipeById(id);
        
        if (!recipe.getName().equals(request.getName()) && 
            recipeRepository.existsByEquipmentTypeSchema_IdAndName(recipe.getEquipmentTypeSchemaId(), request.getName())) {
            throw new RuntimeException("Recipe name already exists for this equipment type");
        }
        
        recipe.setName(request.getName());
        recipe.setParameters(request.getParameters());
        if (request.getIsActive() != null) {
            recipe.setIsActive(request.getIsActive());
        }

        return recipeRepository.save(recipe);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    private EquipmentTypeSchema getSchemaByEquipmentType(String equipmentType) {
        return schemaRepository.findByEquipmentType(equipmentType)
                .orElseThrow(() -> new RuntimeException("Equipment type not found"));
    }
}

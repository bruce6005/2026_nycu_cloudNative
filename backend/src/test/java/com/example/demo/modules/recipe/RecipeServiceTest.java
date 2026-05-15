package com.example.demo.modules.recipe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;
import com.example.demo.modules.recipe.dto.RecipeRequest;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.recipe.repository.RecipeRepository;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private EquipmentTypeSchemaRepository schemaRepository;

    @InjectMocks
    private RecipeService recipeService;

    private EquipmentTypeSchema testSchema;
    private RecipeRequest request;

    @BeforeEach
    void setUp() {
        testSchema = new EquipmentTypeSchema();
        testSchema.setId(1L);
        testSchema.setEquipmentType("THERMAL");

        request = new RecipeRequest();
        request.setName("Fast Bake");
        request.setParameters("{\"temp\": 200}");
        request.setIsActive(true);
    }

    @Test
    void createRecipe_Success() {
        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "Fast Bake")).thenReturn(false);
        when(recipeRepository.findByEquipmentTypeSchema_Id(1L)).thenReturn(Collections.emptyList());
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(i -> i.getArgument(0));

        Recipe result = recipeService.createRecipe("THERMAL", request);

        assertNotNull(result);
        assertEquals("Fast Bake", result.getName());
        assertEquals(testSchema, result.getEquipmentTypeSchema());
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void createRecipe_Fail_DuplicateName() {
        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "Fast Bake")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> recipeService.createRecipe("THERMAL", request));
        assertEquals("Recipe name already exists for this equipment type", exception.getMessage());
    }

    @Test
    void createRecipe_Fail_DuplicateParameters() {
        Recipe existingRecipe = new Recipe();
        existingRecipe.setName("Old Bake");
        existingRecipe.setParameters("{\"temp\": 200}"); // Same as request

        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "Fast Bake")).thenReturn(false);
        when(recipeRepository.findByEquipmentTypeSchema_Id(1L)).thenReturn(Collections.singletonList(existingRecipe));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> recipeService.createRecipe("THERMAL", request));
        assertTrue(exception.getMessage().contains("identical parameters already exists"));
    }

    @Test
    void deleteRecipe_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setIsActive(true);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        
        recipeService.deleteRecipe(1L);
        
        assertFalse(recipe.getIsActive());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void recoverRecipe_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setIsActive(false);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        
        recipeService.recoverRecipe(1L);
        
        assertTrue(recipe.getIsActive());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void getRecipesByEquipmentType_Success() {
        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        
        Recipe recipe = new Recipe();
        when(recipeRepository.findByEquipmentTypeSchema_Id(1L)).thenReturn(Collections.singletonList(recipe));
        
        List<Recipe> result = recipeService.getRecipesByEquipmentType("THERMAL");
        assertEquals(1, result.size());
    }

    @Test
    void getAllRecipes_Success() {
        Recipe recipe = new Recipe();
        when(recipeRepository.findAll()).thenReturn(Collections.singletonList(recipe));
        
        List<Recipe> result = recipeService.getAllRecipes();
        assertEquals(1, result.size());
    }

    @Test
    void getRecipeById_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        
        Recipe result = recipeService.getRecipeById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getRecipeById_NotFound() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> recipeService.getRecipeById(1L));
    }

    @Test
    void createRecipe_Fail_InvalidJson() {
        request.setParameters("invalid json");
        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "Fast Bake")).thenReturn(false);
        when(recipeRepository.findByEquipmentTypeSchema_Id(1L)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> recipeService.createRecipe("THERMAL", request));
        assertTrue(exception.getMessage().contains("Error parsing parameters"));
    }

    @Test
    void updateRecipe_Success() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Old Bake");
        recipe.setEquipmentTypeSchema(testSchema);

        request.setName("New Bake");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "New Bake")).thenReturn(false);
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(i -> i.getArgument(0));

        Recipe result = recipeService.updateRecipe(1L, request);
        assertEquals("New Bake", result.getName());
    }

    @Test
    void updateRecipe_Fail_DuplicateName() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Old Bake");
        recipe.setEquipmentTypeSchema(testSchema);

        request.setName("New Bake");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.existsByEquipmentTypeSchema_IdAndName(1L, "New Bake")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> recipeService.updateRecipe(1L, request));
        assertEquals("Recipe name already exists for this equipment type", exception.getMessage());
    }
}

package com.example.demo.modules.recipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.recipe.model.Recipe;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByEquipmentTypeSchema_Id(Long equipmentTypeSchemaId);
    boolean existsByEquipmentTypeSchema_IdAndName(Long equipmentTypeSchemaId, String name);
}

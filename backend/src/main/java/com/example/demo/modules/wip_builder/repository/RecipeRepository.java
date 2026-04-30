package com.example.demo.modules.wip_builder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByEquipmentId(Long equipmentId);
}

package com.example.demo.modules.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;

import java.util.Optional;

@Repository
public interface EquipmentTypeSchemaRepository extends JpaRepository<EquipmentTypeSchema, Long> {
    Optional<EquipmentTypeSchema> findByEquipmentType(String equipmentType);
}

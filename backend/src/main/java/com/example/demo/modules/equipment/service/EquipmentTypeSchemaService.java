package com.example.demo.modules.equipment.service;

import org.springframework.stereotype.Service;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;

import java.util.List;

@Service
public class EquipmentTypeSchemaService {

    private final EquipmentTypeSchemaRepository repository;

    public EquipmentTypeSchemaService(EquipmentTypeSchemaRepository repository) {
        this.repository = repository;
    }

    public List<EquipmentTypeSchema> getAllSchemas() {
        return repository.findAll();
    }

    public EquipmentTypeSchema getSchemaByType(String type) {
        return repository.findByEquipmentType(type)
                .orElseThrow(() -> new RuntimeException("Schema not found for type: " + type));
    }

    public EquipmentTypeSchema createSchema(EquipmentTypeSchema schema) {
        return repository.save(schema);
    }

    public void deleteSchema(Long id) {
        repository.deleteById(id);
    }
}

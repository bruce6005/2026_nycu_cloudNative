package com.example.demo.modules.equipment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.service.EquipmentTypeSchemaService;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-schemas")
public class EquipmentTypeSchemaController {

    private final EquipmentTypeSchemaService service;

    public EquipmentTypeSchemaController(EquipmentTypeSchemaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<EquipmentTypeSchema>> getAllSchemas() {
        return ResponseEntity.ok(service.getAllSchemas());
    }

    @GetMapping("/{type}")
    public ResponseEntity<EquipmentTypeSchema> getSchemaByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getSchemaByType(type));
    }

    @PostMapping
    public ResponseEntity<EquipmentTypeSchema> createSchema(@RequestBody EquipmentTypeSchema schema) {
        return ResponseEntity.ok(service.createSchema(schema));
    }
}

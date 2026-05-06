package com.example.demo.modules.equipment.service;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EquipmentSeeder implements CommandLineRunner {

    private final EquipmentTypeSchemaRepository schemaRepository;
    private final EquipmentRepository equipmentRepository;

    public EquipmentSeeder(EquipmentTypeSchemaRepository schemaRepository, EquipmentRepository equipmentRepository) {
        this.schemaRepository = schemaRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        EquipmentTypeSchema schema1;
        EquipmentTypeSchema schema2;

        if (schemaRepository.count() == 0) {
            schema1 = new EquipmentTypeSchema();
            schema1.setEquipmentType("KLA_DEFECT_INSPECTION");
            schema1.setParameterSchema("{\"type\": \"object\", \"properties\": {\"magnification\": {\"type\": \"string\", \"enum\": [\"10x\", \"20x\", \"50x\"]}, \"threshold\": {\"type\": \"number\", \"minimum\": 0, \"maximum\": 1}}, \"required\": [\"magnification\", \"threshold\"]}");
            schemaRepository.save(schema1);

            schema2 = new EquipmentTypeSchema();
            schema2.setEquipmentType("ELLIPSOMETER");
            schema2.setParameterSchema("{\"type\": \"object\", \"properties\": {\"wavelength\": {\"type\": \"integer\", \"minimum\": 100, \"maximum\": 1000}, \"angle\": {\"type\": \"integer\"}}, \"required\": [\"wavelength\", \"angle\"]}");
            schemaRepository.save(schema2);
            
            System.out.println("Seeded EquipmentTypeSchema data.");
        } else {
            schema1 = schemaRepository.findByEquipmentType("KLA_DEFECT_INSPECTION")
                    .orElseThrow(() -> new RuntimeException("Missing KLA_DEFECT_INSPECTION schema"));
            schema2 = schemaRepository.findByEquipmentType("ELLIPSOMETER")
                    .orElseThrow(() -> new RuntimeException("Missing ELLIPSOMETER schema"));
        }

        if (equipmentRepository.count() == 0) {
            Equipment eq1 = new Equipment();
            eq1.setName("KLA-Tencor Surfscan 1");
            eq1.setEquipmentTypeSchema(schema1);
            eq1.setMaxCapacity(25);
            equipmentRepository.save(eq1);

            Equipment eq2 = new Equipment();
            eq2.setName("KLA-Tencor Surfscan 2");
            eq2.setEquipmentTypeSchema(schema1);
            eq2.setMaxCapacity(25);
            equipmentRepository.save(eq2);

            Equipment eq3 = new Equipment();
            eq3.setName("Ellipsometer Alpha");
            eq3.setEquipmentTypeSchema(schema2);
            eq3.setMaxCapacity(10);
            equipmentRepository.save(eq3);
            
            System.out.println("Seeded Equipment data.");
        }
    }
}

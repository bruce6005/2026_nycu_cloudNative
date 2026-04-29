package com.example.demo.modules.equipment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.equipment.dto.EquipmentRequest;
import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.repository.EquipmentRepository;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public EquipmentService(EquipmentRepository equipmentRepository, UserRepository userRepository) {
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
    }

    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
    }

    @Transactional
    public Equipment createEquipment(EquipmentRequest request) {
        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setType(request.getType());
        equipment.setMaxCapacity(request.getMaxCapacity());

        if (request.getHandlerId() != null) {
            User handler = userRepository.findById(request.getHandlerId())
                    .orElseThrow(() -> new RuntimeException("Handler not found"));
            equipment.setHandler(handler);
        }

        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = getEquipmentById(id);
        equipment.setName(request.getName());
        equipment.setType(request.getType());
        equipment.setMaxCapacity(request.getMaxCapacity());

        if (request.getHandlerId() != null) {
            User handler = userRepository.findById(request.getHandlerId())
                    .orElseThrow(() -> new RuntimeException("Handler not found"));
            equipment.setHandler(handler);
        } else {
            equipment.setHandler(null);
        }

        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
}

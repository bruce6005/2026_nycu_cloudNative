package com.example.demo.modules.equipment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.equipment.dto.EquipmentRequest;
import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeSchemaRepository schemaRepository;
    private final UserRepository userRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;

    public EquipmentService(
            EquipmentRepository equipmentRepository,
            EquipmentTypeSchemaRepository schemaRepository,
            UserRepository userRepository,
            EquipmentStatusLogsRepository equipmentStatusLogsRepository) {
        this.equipmentRepository = equipmentRepository;
        this.schemaRepository = schemaRepository;
        this.userRepository = userRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
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
        equipment.setEquipmentTypeSchema(resolveSchema(request));
        equipment.setMaxCapacity(request.getMaxCapacity());

        if (request.getHandlerId() != null) {
            User handler = userRepository.findById(request.getHandlerId())
                    .orElseThrow(() -> new RuntimeException("Handler not found"));
            equipment.setHandler(handler);
        }

        Equipment savedEquipment = equipmentRepository.save(equipment);
        updateEquipmentStatus(savedEquipment, "READY");
        return savedEquipment;
    }

    @Transactional
    public Equipment updateEquipment(Long id, EquipmentRequest request) {
        Equipment equipment = getEquipmentById(id);
        equipment.setName(request.getName());
        equipment.setEquipmentTypeSchema(resolveSchema(request));
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
        Equipment equipment = getEquipmentById(id);
        String currentStatus = resolveCurrentEquipmentStatus(id);
        
        if (currentStatus != null && !(currentStatus.equalsIgnoreCase("IDLE") || currentStatus.equalsIgnoreCase("READY") || currentStatus.equalsIgnoreCase("STANDBY"))) {
            throw new RuntimeException("Equipment is currently " + currentStatus + " and cannot be soft deleted");
        }
        
        updateEquipmentStatus(equipment, "OFFLINE");
    }

    @Transactional
    public void recoverEquipment(Long id) {
        Equipment equipment = getEquipmentById(id);
        updateEquipmentStatus(equipment, "READY");
    }

    private String resolveCurrentEquipmentStatus(Long equipmentId) {
        return equipmentStatusLogsRepository
                .findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipmentId)
                .or(() -> equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipmentId))
                .map(EquipmentStatusLogs::getStatus)
                .orElse(null);
    }

    private void updateEquipmentStatus(Equipment equipment, String status) {
        equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipment.getId())
                .ifPresent(log -> {
                    log.setEndTime(LocalDateTime.now());
                    equipmentStatusLogsRepository.save(log);
                });

        EquipmentStatusLogs newLog = new EquipmentStatusLogs();
        newLog.setEquipment(equipment);
        newLog.setStatus(status);
        newLog.setStartTime(LocalDateTime.now());
        equipmentStatusLogsRepository.save(newLog);
    }

    private EquipmentTypeSchema resolveSchema(EquipmentRequest request) {
        if (request.getEquipmentTypeSchemaId() != null) {
            return schemaRepository.findById(request.getEquipmentTypeSchemaId())
                    .orElseThrow(() -> new RuntimeException("Equipment type schema not found"));
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            return schemaRepository.findByEquipmentType(request.getType())
                    .orElseThrow(() -> new RuntimeException("Equipment type schema not found"));
        }

        throw new RuntimeException("Equipment type schema is required");
    }
}

package com.example.demo.modules.equipment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.equipment.dto.EquipmentRequest;
import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private EquipmentTypeSchemaRepository schemaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository equipmentStatusLogsRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private EquipmentTypeSchema testSchema;
    private EquipmentRequest request;

    @BeforeEach
    void setUp() {
        testSchema = new EquipmentTypeSchema();
        testSchema.setId(1L);
        testSchema.setEquipmentType("THERMAL");

        request = new EquipmentRequest();
        request.setName("Oven 1");
        request.setMaxCapacity(10);
    }

    @Test
    void createEquipment_Success_WithSchemaId() {
        request.setEquipmentTypeSchemaId(1L);

        when(schemaRepository.findById(1L)).thenReturn(Optional.of(testSchema));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> i.getArgument(0));

        Equipment result = equipmentService.createEquipment(request);

        assertNotNull(result);
        assertEquals("Oven 1", result.getName());
        assertEquals(testSchema, result.getEquipmentTypeSchema());
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    void createEquipment_Success_WithTypeName() {
        request.setType("THERMAL");

        when(schemaRepository.findByEquipmentType("THERMAL")).thenReturn(Optional.of(testSchema));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> i.getArgument(0));

        Equipment result = equipmentService.createEquipment(request);

        assertNotNull(result);
        assertEquals(testSchema, result.getEquipmentTypeSchema());
    }

    @Test
    void createEquipment_WithHandler() {
        request.setEquipmentTypeSchemaId(1L);
        request.setHandlerId(2L);

        User handler = new User();
        handler.setId(2L);

        when(schemaRepository.findById(1L)).thenReturn(Optional.of(testSchema));
        when(userRepository.findById(2L)).thenReturn(Optional.of(handler));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> i.getArgument(0));

        Equipment result = equipmentService.createEquipment(request);

        assertNotNull(result);
        assertEquals(handler, result.getHandler());
    }

    @Test
    void createEquipment_Fail_NoSchemaInfo() {
        // No schema ID and no type
        RuntimeException exception = assertThrows(RuntimeException.class, () -> equipmentService.createEquipment(request));
        assertEquals("Equipment type schema is required", exception.getMessage());
    }

    @Test
    void getEquipmentById_NotFound() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> equipmentService.getEquipmentById(1L));
    }

    @Test
    void getAllEquipments_Success() {
        Equipment eq = new Equipment();
        eq.setId(1L);
        when(equipmentRepository.findAll()).thenReturn(Collections.singletonList(eq));

        List<Equipment> list = equipmentService.getAllEquipments();
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    void getEquipmentById_Success() {
        Equipment eq = new Equipment();
        eq.setId(1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

        Equipment result = equipmentService.getEquipmentById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void updateEquipment_Success() {
        Equipment existing = new Equipment();
        existing.setId(1L);
        
        request.setEquipmentTypeSchemaId(1L);
        request.setHandlerId(null);
        request.setName("Updated Oven");
        
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(schemaRepository.findById(1L)).thenReturn(Optional.of(testSchema));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> i.getArgument(0));

        Equipment result = equipmentService.updateEquipment(1L, request);
        assertEquals("Updated Oven", result.getName());
        assertNull(result.getHandler());
    }

    @Test
    void deleteEquipment_Success() {
        Equipment eq = new Equipment();
        eq.setId(1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        
        com.example.demo.modules.wip_builder.model.EquipmentStatusLogs log = new com.example.demo.modules.wip_builder.model.EquipmentStatusLogs();
        log.setStatus("READY");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(1L))
            .thenReturn(Optional.of(log));

        equipmentService.deleteEquipment(1L);
        
        verify(equipmentStatusLogsRepository, times(2)).save(any());
    }

    @Test
    void deleteEquipment_Fail_StatusBusy() {
        Equipment eq = new Equipment();
        eq.setId(1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        
        com.example.demo.modules.wip_builder.model.EquipmentStatusLogs log = new com.example.demo.modules.wip_builder.model.EquipmentStatusLogs();
        log.setStatus("BUSY");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(1L))
            .thenReturn(Optional.of(log));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> equipmentService.deleteEquipment(1L));
        assertTrue(ex.getMessage().contains("cannot be soft deleted"));
    }

    @Test
    void recoverEquipment_Success() {
        Equipment eq = new Equipment();
        eq.setId(1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        
        equipmentService.recoverEquipment(1L);
        verify(equipmentStatusLogsRepository).save(any());
    }
}

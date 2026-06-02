package com.example.demo.modules.wip_builder.controller;

import com.example.demo.modules.wip_builder.dto.CreateWIPBatchRequest;
import com.example.demo.modules.wip_builder.dto.EquipmentWithRecipesDTO;
import com.example.demo.modules.wip_builder.dto.PendingSampleDTO;
import com.example.demo.modules.wip_builder.dto.RecipeDTO;
import com.example.demo.modules.wip_builder.service.WIPBuilderService;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WIPBuilderController}.
 */
@ExtendWith(MockitoExtension.class)
class WIPBuilderControllerTest {

        @Mock
        private WIPBuilderService wipBuilderService;

        @InjectMocks
        private WIPBuilderController wipBuilderController;

        // -------------------------------------------------------
        // getPendingSamples()
        // -------------------------------------------------------

        @Test
        @DisplayName("getPendingSamples() - 應成功回傳 pending sample 列表")
        void getPendingSamples_shouldReturnList() {
                // Arrange
                PendingSampleDTO dto1 = new PendingSampleDTO(
                                1L, "B001", "NEW", 100L, "Request #100", "desc", "HIGH", 10L, "RECIPE-10"
                );
                PendingSampleDTO dto2 = new PendingSampleDTO(
                                2L, "B002", "NEW", 100L, "Request #100", "desc", "HIGH", 10L, "RECIPE-10"
                );

                when(wipBuilderService.getPendingSamples()).thenReturn(List.of(dto1, dto2));

                // Act
                List<PendingSampleDTO> result = wipBuilderController.getPendingSamples();

                // Assert
                assertEquals(2, result.size());
                assertEquals("B001", result.get(0).getBarcode());
                assertEquals("B002", result.get(1).getBarcode());
                verify(wipBuilderService, times(1)).getPendingSamples();
        }

        @Test
        @DisplayName("getPendingSamples() - 空列表應回傳空清單")
        void getPendingSamples_emptyList_shouldReturnEmpty() {
                // Arrange
                when(wipBuilderService.getPendingSamples()).thenReturn(Collections.emptyList());

                // Act
                List<PendingSampleDTO> result = wipBuilderController.getPendingSamples();

                // Assert
                assertEquals(0, result.size());
                verify(wipBuilderService, times(1)).getPendingSamples();
        }

        // -------------------------------------------------------
        // getEquipments()
        // -------------------------------------------------------

        @Test
        @DisplayName("getEquipments() - 應成功回傳機台清單")
        void getEquipments_shouldReturnList() {
                // Arrange
                RecipeDTO recipe1 = new RecipeDTO();
                recipe1.setId(10L);
                recipe1.setName("RECIPE-10");

                EquipmentWithRecipesDTO eq1 = new EquipmentWithRecipesDTO();
                eq1.setId(1L);
                eq1.setName("EQ-1");
                eq1.setEquipmentType("TYPE_A");
                eq1.setMaxCapacity(10);
                eq1.setCurrentStatus("FREE");
                eq1.setRecipes(List.of(recipe1));

                when(wipBuilderService.getEquipmentsWithRecipes()).thenReturn(List.of(eq1));

                // Act
                List<EquipmentWithRecipesDTO> result = wipBuilderController.getEquipments();

                // Assert
                assertEquals(1, result.size());
                assertEquals(1L, result.get(0).getId());
                assertEquals("EQ-1", result.get(0).getName());
                assertEquals("TYPE_A", result.get(0).getEquipmentType());
                assertEquals(10, result.get(0).getMaxCapacity());
                assertEquals("FREE", result.get(0).getCurrentStatus());
                assertEquals(1, result.get(0).getRecipes().size());
                verify(wipBuilderService, times(1)).getEquipmentsWithRecipes();
        }

        @Test
        @DisplayName("getEquipments() - 空列表應回傳空清單")
        void getEquipments_emptyList_shouldReturnEmpty() {
                // Arrange
                when(wipBuilderService.getEquipmentsWithRecipes()).thenReturn(Collections.emptyList());

                // Act
                List<EquipmentWithRecipesDTO> result = wipBuilderController.getEquipments();

                // Assert
                assertEquals(0, result.size());
                verify(wipBuilderService, times(1)).getEquipmentsWithRecipes();
        }

        // -------------------------------------------------------
        // createWIPBatch()
        // -------------------------------------------------------

        @Test
        @DisplayName("createWIPBatch() - 成功建立批次應回傳 DTO")
        void createWIPBatch_success_shouldReturnDTO() {
                // Arrange
                CreateWIPBatchRequest request = new CreateWIPBatchRequest();
                request.setOperatorId(10L);
                request.setEquipmentId(1L);
                request.setRecipeId(2L);
                request.setSampleIds(List.of(100L, 101L));

                WIPBatchDTO response = new WIPBatchDTO();
                response.setId(5000L);
                response.setStatus("QUEUED");
                response.setEquipmentId(1L);
                response.setEquipmentName("EQ-1");
                response.setRecipeId(2L);
                response.setRecipeName("RECIPE-2");
                response.setCreateTime(LocalDateTime.now());
                response.setSampleBarcodes(List.of("B100", "B101"));

                when(wipBuilderService.createWIPBatch(any(CreateWIPBatchRequest.class))).thenReturn(response);

                // Act
                WIPBatchDTO result = wipBuilderController.createWIPBatch(request);

                // Assert
                assertNotNull(result);
                assertEquals(5000L, result.getId());
                assertEquals("QUEUED", result.getStatus());
                assertEquals(1L, result.getEquipmentId());
                assertEquals("EQ-1", result.getEquipmentName());
                assertEquals(2L, result.getRecipeId());
                assertEquals("RECIPE-2", result.getRecipeName());
                assertEquals(2, result.getSampleBarcodes().size());
                verify(wipBuilderService, times(1)).createWIPBatch(any(CreateWIPBatchRequest.class));
        }

        @Test
        @DisplayName("createWIPBatch() - Service 拋例外時應傳播例外")
        void createWIPBatch_serviceThrows_shouldThrowException() {
                // Arrange
                CreateWIPBatchRequest request = new CreateWIPBatchRequest();
                request.setOperatorId(10L);
                request.setEquipmentId(999L);
                request.setRecipeId(999L);
                request.setSampleIds(List.of(100L));

                when(wipBuilderService.createWIPBatch(any(CreateWIPBatchRequest.class)))
                                .thenThrow(new RuntimeException("Equipment not found"));

                // Act & Assert
                assertThrows(RuntimeException.class, () -> wipBuilderController.createWIPBatch(request),
                                "Service 拋例外時 Controller 應傳播例外");

                verify(wipBuilderService, times(1)).createWIPBatch(any(CreateWIPBatchRequest.class));
        }
}

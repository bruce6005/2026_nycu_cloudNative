package com.example.demo.modules.wip_builder.service;

import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.equipment.model.EquipmentTypeSchema;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.wip_builder.dto.CreateWIPBatchRequest;
import com.example.demo.modules.wip_builder.dto.PendingSampleDTO;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.wip_builder.model.WIPbatch;
import com.example.demo.modules.wip_builder.model.TestRecords;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.wip_builder.repository.TestRecordsRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.recipe.repository.RecipeRepository;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.notification.service.NotificationService;

import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WIPBuilderServiceTest {

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private WIPbatchRepository wipbatchRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private TestRecordsRepository testRecordsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private WIPBuilderService wipBuilderService;

    // Helpers
    private EquipmentTypeSchema buildSchema(Long id, String type) {
        EquipmentTypeSchema s = new EquipmentTypeSchema();
        s.setId(id);
        s.setEquipmentType(type);
        s.setParameterSchema("{}");
        return s;
    }

    private Equipment buildEquipment(Long id, int capacity, Long schemaId) {
        Equipment e = new Equipment();
        e.setId(id);
        e.setName("EQ-" + id);
        e.setMaxCapacity(capacity);
        e.setEquipmentTypeSchema(buildSchema(schemaId, "TYPE" + schemaId));
        return e;
    }

    private Recipe buildRecipe(Long id, Long schemaId) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setName("RECIPE-" + id);
        r.setEquipmentTypeSchema(buildSchema(schemaId, "TYPE" + schemaId));
        r.setParameters("{}");
        r.setIsActive(true);
        return r;
    }

    private Request buildRequest(Long id, String status) {
        Request req = new Request();
        req.setId(id);
        req.setStatus(status);
        req.setDescription("desc");
        return req;
    }

    private Sample buildSample(Long id, Request req, Recipe recipe, String status, WIPbatch batch) {
        Sample s = new Sample();
        s.setId(id);
        s.setBarcode("B" + id);
        s.setRequest(req);
        s.setRecipe(recipe);
        s.setStatus(status);
        s.setBatch(batch);
        return s;
    }

    // -------------------------------------------------------
    // getPendingSamples()
    // -------------------------------------------------------

    @Test
    @DisplayName("getPendingSamples() - 正常情況應回傳正確轉換的 DTO")
    void getPendingSamples_returnsMappedDto() {
        // Arrange
        Request request = buildRequest(1L, "APPROVED");
        Recipe recipe = buildRecipe(1L, 100L);
        Sample sample = buildSample(1L, request, recipe, "NEW", null);
        
        when(sampleRepository.findByBatchIsNull()).thenReturn(List.of(sample));

        // Act
        List<PendingSampleDTO> result = wipBuilderService.getPendingSamples();

        // Assert
        assertEquals(1, result.size());
        assertEquals(sample.getId(), result.get(0).getSampleId());
        assertEquals(recipe.getId(), result.get(0).getRecipeId());
    }

    @Test
    @DisplayName("getPendingSamples() - 應過濾掉 Request 狀態為 PENDING 的 Sample")
    void getPendingSamples_excludesNotDispatchableRequests() {
        // Arrange
        Request pendingRequest = buildRequest(2L, "PENDING"); // Request為PENDING 不可派工
        Recipe recipe = buildRecipe(1L, 100L);
        Sample sample = buildSample(3L, pendingRequest, recipe, "NEW", null);
        
        when(sampleRepository.findByBatchIsNull()).thenReturn(List.of(sample));

        // Act
        List<PendingSampleDTO> result = wipBuilderService.getPendingSamples();

        // Assert
        assertEquals(0, result.size(), "不合法的 Request 狀態應該被過濾掉");
    }
    // -------------------------------------------------------
    // createWIPBatch()
    // -------------------------------------------------------

    @Test
    @DisplayName("createWIPBatch() - 空 sampleIds 應拋例外")
    void createWIPBatch_emptySampleIds_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(1L);
        req.setEquipmentId(1L);
        req.setRecipeId(1L);
        req.setSampleIds(List.of());

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req));
    }

    @Test
    @DisplayName("createWIPBatch() - 成功建立批次並廣播通知")
    void createWIPBatch_success_shouldCreateBatchAndBroadcast() {
        // setup
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        req.setSampleIds(List.of(100L, 101L));

        Equipment eq = buildEquipment(1L, 10, 500L);
        Recipe recipe = buildRecipe(2L, 500L);

        Request request = buildRequest(200L, "APPROVED");

        Sample s1 = buildSample(100L, request, recipe, "NEW", null);
        Sample s2 = buildSample(101L, request, recipe, "NEW", null);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));
        when(sampleRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(s1, s2));

        WIPbatch saved = new WIPbatch();
        saved.setId(5000L);
        saved.setEquipment(eq);
        saved.setRecipe(recipe);
        saved.setStatus("QUEUED");
        when(wipbatchRepository.save(any(WIPbatch.class))).thenReturn(saved);

        User operator = new User();
        operator.setId(10L);
        operator.setEmail("op@example.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(operator));

        when(testRecordsRepository.save(any(TestRecords.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sampleRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        // After assigning, checkAndUpdateRequestStatus will call sampleRepository.findByRequest_Id
        Sample assigned1 = buildSample(100L, request, recipe, "ASSIGNED", null);
        Sample assigned2 = buildSample(101L, request, recipe, "ASSIGNED", null);
        when(sampleRepository.findByRequest_Id(200L)).thenReturn(List.of(assigned1, assigned2));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // act
        var dto = wipBuilderService.createWIPBatch(req);

        // verify
        assertNotNull(dto);
        assertEquals(saved.getId(), dto.getId());
        verify(wipbatchRepository, times(1)).save(any(WIPbatch.class));
        verify(notificationService, atLeastOnce()).broadcast(eq("REQUEST_UPDATED"), anyString());
        verify(testRecordsRepository, times(1)).save(any(TestRecords.class));
    }

    @Test
    @DisplayName("createWIPBatch() - 機台不符合 recipe 應拋例外")
    void createWIPBatch_equipmentMismatchRecipe_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        req.setSampleIds(List.of(100L));

        // Equipment 使用 schema 100，Recipe 使用 schema 200
        Equipment eq = buildEquipment(1L, 10, 100L);
        Recipe recipe = buildRecipe(2L, 200L);  // 不同的 schema

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req),
                "Recipe 的設備類型與機台不符應拋例外");
    }

    @Test
    @DisplayName("createWIPBatch() - Sample 數量超出機台容量應拋例外")
    void createWIPBatch_sampleCountExceedsCapacity_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        // 要派 3 個 samples，但機台容量只有 2
        req.setSampleIds(List.of(100L, 101L, 102L));

        Equipment eq = buildEquipment(1L, 2, 500L);  // maxCapacity = 2
        Recipe recipe = buildRecipe(2L, 500L);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req),
                "Sample 數量超出機台容量應拋例外");
    }

    @Test
    @DisplayName("createWIPBatch() - 設備為 BUSY 應拋例外")
    void createWIPBatch_equipmentBusy_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(1L);
        req.setEquipmentId(1L);
        req.setRecipeId(1L);
        req.setSampleIds(List.of(1L));

        Equipment eq = buildEquipment(1L, 5, 10L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));

        EquipmentStatusLogs log = new EquipmentStatusLogs();
        log.setStatus("BUSY");
        when(equipmentStatusLogsRepository.findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(1L))
                .thenReturn(Optional.of(log));

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req));
    }

    @Test
    @DisplayName("createWIPBatch() - 樣品已經在其他批次中應拋例外")
    void createWIPBatch_sampleAlreadyHasBatch_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        req.setSampleIds(List.of(100L));

        Equipment eq = buildEquipment(1L, 10, 500L);
        Recipe recipe = buildRecipe(2L, 500L);
        Request request = buildRequest(200L, "APPROVED");

        WIPbatch existingBatch = new WIPbatch();
        existingBatch.setId(999L);
        Sample sampleWithBatch = buildSample(100L, request, recipe, "ASSIGNED", existingBatch);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));
        when(sampleRepository.findAllById(List.of(100L))).thenReturn(List.of(sampleWithBatch));

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req));
    }

    @Test
    @DisplayName("createWIPBatch() - 樣品配方與批次配方不一致應拋例外")
    void createWIPBatch_sampleRecipeMismatch_shouldThrow() {
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        req.setSampleIds(List.of(100L, 101L));

        Equipment eq = buildEquipment(1L, 10, 500L);
        Recipe recipe2 = buildRecipe(2L, 500L);
        Recipe wrongRecipe = buildRecipe(99L, 500L); // 另一個配方
        Request request = buildRequest(200L, "APPROVED");

        Sample s1 = buildSample(100L, request, recipe2, "NEW", null);
        Sample s2 = buildSample(101L, request, wrongRecipe, "NEW", null);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe2));
        when(sampleRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(s1, s2));

        assertThrows(RuntimeException.class, () -> wipBuilderService.createWIPBatch(req));
    }

    @Test
    @DisplayName("createWIPBatch() - 模擬資料庫死鎖，應拋出 CannotAcquireLockException")
    void createWIPBatch_databaseDeadlock_shouldThrowException() {
        // Arrange: 準備合法的 request/equipment/recipe/samples，使流程能走到 sampleRepository.saveAll
        CreateWIPBatchRequest req = new CreateWIPBatchRequest();
        req.setOperatorId(10L);
        req.setEquipmentId(1L);
        req.setRecipeId(2L);
        req.setSampleIds(List.of(100L, 101L));

        Equipment eq = buildEquipment(1L, 10, 500L);
        Recipe recipe = buildRecipe(2L, 500L);
        Request request = buildRequest(200L, "APPROVED");
        Sample s1 = buildSample(100L, request, recipe, "NEW", null);
        Sample s2 = buildSample(101L, request, recipe, "NEW", null);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(eq));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));
        when(sampleRepository.findAllById(List.of(100L, 101L))).thenReturn(List.of(s1, s2));

        WIPbatch saved = new WIPbatch();
        saved.setId(5000L);
        saved.setEquipment(eq);
        saved.setRecipe(recipe);
        saved.setStatus("QUEUED");
        when(wipbatchRepository.save(any(WIPbatch.class))).thenReturn(saved);

        User operator = new User();
        operator.setId(10L);
        operator.setEmail("op@example.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(operator));

        when(testRecordsRepository.save(any(TestRecords.class))).thenAnswer(inv -> inv.getArgument(0));
        // Fault injection: 模擬資料庫死鎖於 saveAll
        when(sampleRepository.saveAll(any())).thenThrow(new org.springframework.dao.CannotAcquireLockException("Simulated Database Deadlock"));

        // Act & Assert: 確認例外被向上拋出
        assertThrows(org.springframework.dao.CannotAcquireLockException.class, () -> wipBuilderService.createWIPBatch(req));
    }
}

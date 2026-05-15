package com.example.demo.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.auth.model.UserRole;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.equipment.repository.EquipmentTypeSchemaRepository;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.recipe.repository.RecipeRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.wip_builder.repository.TestRecordsRepository;

@Component
@Transactional
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeSchemaRepository equipmentTypeSchemaRepository;
    private final RecipeRepository recipeRepository;
    private final RequestRepository requestRepository;
    private final SampleRepository sampleRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final WIPbatchRepository wipbatchRepository;
    private final boolean resetDataOnStartup;
    private final TestRecordsRepository testRecordsRepository;

    public DataInitializer(UserRepository userRepository,
                           EquipmentRepository equipmentRepository,
                           EquipmentTypeSchemaRepository equipmentTypeSchemaRepository,
                           RecipeRepository recipeRepository,
                           RequestRepository requestRepository,
                           SampleRepository sampleRepository,
                           EquipmentStatusLogsRepository equipmentStatusLogsRepository,
                           WIPbatchRepository wipbatchRepository,
                           TestRecordsRepository testRecordsRepository,
                           @Value("${app.reset-data-on-startup:true}") boolean resetDataOnStartup) {
        this.userRepository = userRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentTypeSchemaRepository = equipmentTypeSchemaRepository;
        this.recipeRepository = recipeRepository;
        this.requestRepository = requestRepository;
        this.sampleRepository = sampleRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.wipbatchRepository = wipbatchRepository;
        this.testRecordsRepository = testRecordsRepository;
        this.resetDataOnStartup = resetDataOnStartup;
    }

    @Override
    public void run(String... args) {
        if (!resetDataOnStartup) {
            return;
        }

        clearSeedTables();
        seedUsers();
        seedEquipmentTypeSchemas();
        seedEquipments();
        seedRecipes();
        seedRequestsAndSamples();
        seedWIPBatches();
        seedEquipmentStatusLogs();
    }

    private void clearSeedTables() {
        testRecordsRepository.deleteAllInBatch();

        sampleRepository.deleteAllInBatch();

        wipbatchRepository.deleteAllInBatch();

        equipmentStatusLogsRepository.deleteAllInBatch();

        recipeRepository.deleteAllInBatch();

        requestRepository.deleteAllInBatch();

        equipmentRepository.deleteAllInBatch();

        equipmentTypeSchemaRepository.deleteAllInBatch();

        userRepository.deleteAllInBatch();
    }

    private void seedUsers() {
        execute(
            "INSERT INTO users (id, google_id, email, name, avatar_url, role, manager_id) VALUES (?, ?, ?, ?, NULL, ?, ?)",
            1L, "seed-google-1", "allan@example.com", "Factory User", UserRole.REQUESTER.name(), 2L
        );
        execute(
            "INSERT INTO users (id, google_id, email, name, avatar_url, role, manager_id) VALUES (?, ?, ?, ?, NULL, ?, ?)",
            2L, "seed-google-2", "manager@example.com", "Manager1", UserRole.MANAGER.name(), null
        );
    }

    private void seedEquipmentTypeSchemas() {
        Object[][] schemaRows = new Object[][] {
                {1L, "THERMAL", "{\"type\": \"object\", \"properties\": {\"temperature\": {\"type\": \"integer\", \"minimum\": 0, \"maximum\": 1200}, \"time_minutes\": {\"type\": \"integer\", \"minimum\": 1, \"maximum\": 600}}, \"required\": [\"temperature\", \"time_minutes\"]}"},
                {2L, "COATING", "{\"type\": \"object\", \"properties\": {\"spin_speed_rpm\": {\"type\": \"integer\", \"minimum\": 100, \"maximum\": 5000}, \"thickness_nm\": {\"type\": \"integer\", \"minimum\": 1, \"maximum\": 1000}}, \"required\": [\"spin_speed_rpm\"]}"},
                {3L, "ETCHING", "{\"type\": \"object\", \"properties\": {\"gas_flow_sccm\": {\"type\": \"integer\", \"minimum\": 1, \"maximum\": 500}, \"rf_power_w\": {\"type\": \"integer\", \"minimum\": 10, \"maximum\": 2000}}, \"required\": [\"gas_flow_sccm\", \"rf_power_w\"]}"},
                {4L, "INSPECT", "{\"type\": \"object\", \"properties\": {\"magnification\": {\"type\": \"string\", \"enum\": [\"10x\", \"20x\", \"50x\"]}, \"threshold\": {\"type\": \"number\", \"minimum\": 0, \"maximum\": 1}}, \"required\": [\"magnification\", \"threshold\"]}"},
                {5L, "UV_CURE", "{\"type\": \"object\", \"properties\": {\"temperature\": {\"type\": \"integer\", \"minimum\": 0, \"maximum\": 100}, \"time_minutes\": {\"type\": \"integer\", \"minimum\": 1, \"maximum\": 60}}, \"required\": [\"temperature\", \"time_minutes\"]}"},
        };

        for (Object[] row : schemaRows) {
            execute(
                    "INSERT INTO equipment_type_schema (id, equipment_type, parameter_schema) VALUES (?, ?, ?)",
                    row[0], row[1], row[2]
            );
        }
    }

    private void seedEquipments() {
        Object[][] equipmentRows = new Object[][] {
            {1L, 2L, "High-Temp Oven 1 (高溫烤箱)", "THERMAL", 1L, 10},
            {2L, 2L, "Spin Coater 1 (光阻塗佈機)", "COATING", 2L, 1},
            {3L, 2L, "Plasma Etcher 1 (電漿蝕刻機)", "ETCHING", 3L, 4},
            {4L, 2L, "SEM Microscope 1 (電子顯微鏡)", "INSPECT", 4L, 5},
            {5L, 2L, "UV Curing 1 (UV固化機)", "UV_CURE", 5L, 5},
            {6L, 2L, "High-Temp Oven 2 (高溫烤箱)", "THERMAL", 1L, 8},
            {7L, 2L, "Spin Coater 2 (光阻塗佈機)", "COATING", 2L, 1},
            {8L, 2L, "Plasma Etcher 2 (電漿蝕刻機)", "ETCHING", 3L, 4},
            {9L, 2L, "SEM Microscope 2 (電子顯微鏡)", "INSPECT", 4L, 10},
            {10L, 2L, "UV Curing 2 (UV固化機)", "UV_CURE", 5L, 5},
        };

        for (Object[] row : equipmentRows) {
            execute(
                    "INSERT INTO equipment (id, handler_id, name, equipment_type_schema_id, max_capacity) VALUES (?, ?, ?, ?, ?)",
                    row[0], row[1], row[2], row[4], row[5]
            );
        }
    }

    private void seedRecipes() {
        Object[][] recipeRows = new Object[][] {
                {1L, 1L, "BAKE_180C_30M", "{\"temperature\": 180, \"time_minutes\": 30}"},
                {2L, 1L, "BAKE_120C_10M", "{\"temperature\": 120, \"time_minutes\": 10}"},
                {3L, 2L, "COAT_SU8_500RPM", "{\"spin_speed_rpm\": 500, \"thickness_nm\": 100}"},
                {4L, 3L, "ETCH_OXIDE_CF4", "{\"gas_flow_sccm\": 100, \"rf_power_w\": 500}"},
                {5L, 4L, "INSPECT_10NM_RES", "{\"magnification\": \"50x\", \"threshold\": 0.5}"},
                {6L, 1L, "UV_CURE_STANDARD", "{\"temperature\": 25, \"time_minutes\": 5}"},
        };

        for (Object[] row : recipeRows) {
            execute(
                    "INSERT INTO recipe (id, equipment_type_schema_id, name, parameters, is_active) VALUES (?, ?, ?, ?, true)",
                    row[0], row[1], row[2], row[3]
            );
        }
    }

    private void seedRequestsAndSamples() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 9, 0);
        int barcodeIndex = 1;
        List<Long> recipeIds = recipeRepository.findAll().stream()
                .map(recipe -> recipe.getId())
                .toList();

        if (recipeIds.isEmpty()) {
            throw new IllegalStateException("No recipes available for sample seeding");
        }

        RequestSeed[] seeds = new RequestSeed[] {
            new RequestSeed(1L, "URGENT", 4),
            new RequestSeed(2L, "NORMAL", 5),
            new RequestSeed(3L, "NORMAL", 3),
            new RequestSeed(4L, "NORMAL", 8),
            new RequestSeed(5L, "URGENT", 2),
            new RequestSeed(6L, "NORMAL", 2),
            new RequestSeed(7L, "NORMAL", 2),
            new RequestSeed(8L, "URGENT", 2),
            new RequestSeed(9L, "NORMAL", 2),
            new RequestSeed(10L, "NORMAL", 1),
            new RequestSeed(11L, "URGENT", 1),
            new RequestSeed(12L, "NORMAL", 1),
            new RequestSeed(13L, "NORMAL", 1),
            new RequestSeed(14L, "URGENT", 1),
            new RequestSeed(15L, "NORMAL", 1),
            new RequestSeed(16L, "NORMAL", 1),
            new RequestSeed(17L, "NORMAL", 1),
            new RequestSeed(18L, "URGENT", 2),
            new RequestSeed(19L, "NORMAL", 2),
            new RequestSeed(20L, "NORMAL", 3),
        };

        for (RequestSeed seed : seeds) {
            Long recipeId = recipeIds.get(ThreadLocalRandom.current().nextInt(recipeIds.size()));

            execute(
                    "INSERT INTO request (id, title, factory_user_id, approver_id, priority, status, create_time, end_time, draft_content, description) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, NULL, ?)",
                    seed.id,
                    "Integrated Seed Request " + seed.id,
                    1L,
                    2L,
                    seed.priority,
                    "APPROVED",
                    baseTime.plusMinutes(seed.id * 7L),
                    "Semiconductor lab seed request " + seed.id
            );

            for (int sampleNo = 1; sampleNo <= seed.sampleCount; sampleNo++) {
                execute(
                    "INSERT INTO sample (id, request_id, batch_id, barcode, recipe_id, status) VALUES (?, ?, NULL, ?, ?, ?)",
                        (long) barcodeIndex,
                        seed.id,
                        String.format("WAFER-%03d", barcodeIndex),
                        recipeId,
                        "PENDING"
                );
                barcodeIndex++;
            }
        }
    }

    private void seedEquipmentStatusLogs() {
        Object[][] logs = new Object[][] {
                {1L, 1L, "READY", LocalDateTime.of(2026, 4, 1, 8, 0)},
                {2L, 2L, "READY", LocalDateTime.of(2026, 4, 1, 8, 5)},
                {3L, 3L, "BUSY", LocalDateTime.of(2026, 4, 1, 8, 10)},
                {4L, 4L, "IDLE", LocalDateTime.of(2026, 4, 1, 8, 15)},
                {5L, 5L, "MAINTENANCE", LocalDateTime.of(2026, 4, 1, 8, 20)},
        };

        for (Object[] row : logs) {
            execute(
                    "INSERT INTO equipment_status_logs (id, equipment_id, status, start_time, end_time) VALUES (?, ?, ?, ?, NULL)",
                    row[0], row[1], row[2], row[3]
            );
        }
    }

    private void seedWIPBatches() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 10, 0);
        List<Long> sampleIds = sampleRepository.findAll().stream()
                .map(sample -> sample.getId())
                .toList();

        if (sampleIds.isEmpty()) {
            System.out.println("No samples available for WIP batch seeding");
            return;
        }

        // Batch 1: QUEUED - waiting to start
        execute(
                "INSERT INTO wip_batch (id, recipe_id, equipment_id, status, create_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, NULL, NULL)",
                1L, 1L, 1L, "QUEUED", baseTime
        );
        // Assign first 4 samples to batch 1
        updateSampleBatch(1L, 4L, 1L);

        // Batch 2: RUNNING - currently processing
        LocalDateTime runStartTime = baseTime.plusHours(1);
        execute(
                "INSERT INTO wip_batch (id, recipe_id, equipment_id, status, create_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, NULL)",
                2L, 2L, 2L, "RUNNING", baseTime.plusMinutes(15), runStartTime
        );
        // Assign samples 5-9 to batch 2, mark as PROCESSING
        for (long i = 5; i <= 9; i++) {
            if (i <= sampleIds.size()) {
                execute(
                        "UPDATE sample SET batch_id = ?, status = ? WHERE id = ?",
                        2L, "PROCESSING", i
                );
            }
        }

        // Batch 3: FINISHED - completed successfully
        LocalDateTime finishTime = baseTime.minusHours(2);
        execute(
                "INSERT INTO wip_batch (id, recipe_id, equipment_id, status, create_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                3L, 3L, 3L, "FINISHED", finishTime.minusHours(1), finishTime.minusMinutes(30), finishTime
        );
        // Assign samples 10-12 to batch 3, mark as COMPLETED
        for (long i = 10; i <= 12; i++) {
            if (i <= sampleIds.size()) {
                execute(
                        "UPDATE sample SET batch_id = ?, status = ? WHERE id = ?",
                        3L, "COMPLETED", i
                );
            }
        }

        // Batch 4: FINISHED - another completed batch with different recipe
        LocalDateTime finishTime2 = baseTime.minusHours(3);
        execute(
                "INSERT INTO wip_batch (id, recipe_id, equipment_id, status, create_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?)",
                4L, 4L, 4L, "FINISHED", finishTime2.minusHours(1), finishTime2.minusMinutes(45), finishTime2
        );
        // Assign samples 13-15 to batch 4
        for (long i = 13; i <= 15; i++) {
            if (i <= sampleIds.size()) {
                execute(
                        "UPDATE sample SET batch_id = ?, status = ? WHERE id = ?",
                        4L, "COMPLETED", i
                );
            }
        }

        // Batch 5: QUEUED - another waiting batch
        execute(
                "INSERT INTO wip_batch (id, recipe_id, equipment_id, status, create_time, start_time, end_time) VALUES (?, ?, ?, ?, ?, NULL, NULL)",
                5L, 5L, 5L, "QUEUED", baseTime.plusHours(2)
        );
        // Assign samples 16-18 to batch 5
        for (long i = 16; i <= 18; i++) {
            if (i <= sampleIds.size()) {
                execute(
                        "UPDATE sample SET batch_id = ? WHERE id = ?",
                        5L, i
                );
            }
        }
    }

    private void updateSampleBatch(Long startId, Long endId, Long batchId) {
        for (long i = startId; i <= endId; i++) {
            if (i <= sampleRepository.findAll().size()) {
                execute(
                        "UPDATE sample SET batch_id = ? WHERE id = ?",
                        batchId, i
                );
            }
        }
    }

    private void execute(String sql, Object... params) {
        var query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        query.executeUpdate();
    }

    private record RequestSeed(Long id, String priority, int sampleCount) {
    }
}

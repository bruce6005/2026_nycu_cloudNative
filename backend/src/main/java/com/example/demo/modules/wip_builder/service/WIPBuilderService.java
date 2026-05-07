package com.example.demo.modules.wip_builder.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.wip_builder.dto.CreateWIPBatchRequest;
import com.example.demo.modules.wip_builder.dto.EquipmentWithRecipesDTO;
import com.example.demo.modules.wip_builder.dto.PendingSamplesGroupedByRequestDTO;
import com.example.demo.modules.wip_builder.dto.RecipeDTO;
import com.example.demo.modules.wip_management.dto.WIPBatchDTO;
import com.example.demo.modules.equipment.repository.EquipmentRepository;
import com.example.demo.modules.wip_builder.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.recipe.repository.RecipeRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.wip_builder.repository.WIPbatchRepository;
import com.example.demo.modules.equipment.model.Equipment;
import com.example.demo.modules.wip_builder.model.EquipmentStatusLogs;
import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.wip_builder.model.WIPbatch;

@Service
public class WIPBuilderService {

    private final SampleRepository sampleRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final RecipeRepository recipeRepository;
    private final WIPbatchRepository wipbatchRepository;

    public WIPBuilderService(SampleRepository sampleRepository,
            EquipmentRepository equipmentRepository,
            EquipmentStatusLogsRepository equipmentStatusLogsRepository,
            RecipeRepository recipeRepository,
            WIPbatchRepository wipbatchRepository) {
        this.sampleRepository = sampleRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentStatusLogsRepository = equipmentStatusLogsRepository;
        this.recipeRepository = recipeRepository;
        this.wipbatchRepository = wipbatchRepository;
    }

    @Transactional(readOnly = true)
    public List<PendingSamplesGroupedByRequestDTO> getPendingSamplesGroupedByRequest() {
        // Map<Long, List<Sample>> groupedSamples =
        // sampleRepository.findByBatchIsNull().stream()
        // .filter(sample -> isDispatchableRequest(sample.getRequest()))
        List<Sample> allSamples = sampleRepository.findByBatchIsNull();
        System.out.println("[DEBUG] Total samples without batch: " + allSamples.size());

        Map<Long, List<Sample>> groupedSamples = allSamples.stream()
                .filter(sample -> {
                    boolean dispatchable = isDispatchableRequest(sample.getRequest());
                    if (dispatchable) {
                        System.out.println(
                                "[DEBUG] Sample " + sample.getBarcode() + " of Request " + sample.getRequest().getId()
                                        + " is dispatchable (Status: " + sample.getRequest().getStatus() + ")");
                    }
                    return dispatchable;
                })
                //
                .collect(Collectors.groupingBy(sample -> sample.getRequest().getId(), LinkedHashMap::new,
                        Collectors.toList()));

        List<PendingSamplesGroupedByRequestDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Sample>> entry : groupedSamples.entrySet()) {
            List<Sample> samples = entry.getValue();
            Request request = samples.get(0).getRequest();

            PendingSamplesGroupedByRequestDTO dto = new PendingSamplesGroupedByRequestDTO();
            dto.setRequestId(request.getId());
            dto.setRequestTitle(resolveRequestTitle(request));
            dto.setRequestDescription(request.getDescription());
            dto.setPriority(request.getPriority());
            dto.setPendingSampleCount(samples.size());
            dto.setUnassignedSampleIds(samples.stream().map(Sample::getId).toList());
            result.add(dto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<EquipmentWithRecipesDTO> getEquipmentsWithRecipes() {
        return equipmentRepository.findAll().stream()
                .map(this::toEquipmentWithRecipesDTO)
                .toList();
    }

    @Transactional
    public WIPBatchDTO createWIPBatch(CreateWIPBatchRequest request) {
        if (request.getSampleIds() == null || request.getSampleIds().isEmpty()) {
            throw new RuntimeException("sampleIds is required");
        }

        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        if (!recipe.getEquipmentTypeSchema().getId().equals(equipment.getEquipmentTypeSchema().getId())) {
            throw new RuntimeException("Recipe does not belong to the selected equipment");
        }

        if (request.getSampleIds().size() > equipment.getMaxCapacity()) {
            throw new RuntimeException("Dispatch count (" + request.getSampleIds().size() +
                    ") exceeds equipment maximum capacity (" + equipment.getMaxCapacity() + ")");
        }

        List<Sample> samples = sampleRepository.findAllById(request.getSampleIds());
        if (samples.size() != request.getSampleIds().size()) {
            throw new RuntimeException("Some samples were not found");
        }

        for (Sample sample : samples) {
            if (sample.getBatch() != null) {
                throw new RuntimeException("Sample " + sample.getId() + " is already assigned to a batch");
            }
        }

        WIPbatch batch = new WIPbatch();
        batch.setRecipe(recipe);
        batch.setEquipment(equipment);
        batch.setStatus("QUEUED");
        batch.setCreateTime(LocalDateTime.now());

        WIPbatch savedBatch = wipbatchRepository.save(batch);

        for (Sample sample : samples) {
            sample.setBatch(savedBatch);
            sample.setStatus("ASSIGNED");
        }
        sampleRepository.saveAll(samples);

        return toWIPBatchDTO(savedBatch);
    }

    private EquipmentWithRecipesDTO toEquipmentWithRecipesDTO(Equipment equipment) {
        EquipmentWithRecipesDTO dto = new EquipmentWithRecipesDTO();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setMaxCapacity(equipment.getMaxCapacity());
        dto.setCurrentStatus(resolveCurrentEquipmentStatus(equipment.getId()));
        dto.setRecipes(
                recipeRepository.findByEquipmentTypeSchema_Id(equipment.getEquipmentTypeSchema().getId()).stream()
                        .map(this::toRecipeDTO)
                        .toList());
        return dto;
    }

    private RecipeDTO toRecipeDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        return dto;
    }

    private WIPBatchDTO toWIPBatchDTO(WIPbatch batch) {
        WIPBatchDTO dto = new WIPBatchDTO();
        dto.setId(batch.getId());
        dto.setRecipeId(batch.getRecipe().getId());
        dto.setRecipeName(batch.getRecipe().getName());
        dto.setEquipmentId(batch.getEquipment().getId());
        dto.setEquipmentName(batch.getEquipment().getName());
        dto.setStatus(batch.getStatus());
        dto.setCreateTime(batch.getCreateTime());
        dto.setStartTime(batch.getStartTime());
        dto.setEndTime(batch.getEndTime());

        // Fill sample barcodes
        List<Sample> samples = sampleRepository.findByBatch_Id(batch.getId());
        dto.setSampleBarcodes(samples.stream().map(Sample::getBarcode).toList());

        return dto;
    }

    private String resolveRequestTitle(Request request) {
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            return request.getTitle();
        }
        return "Request #" + request.getId();
    }

    private String resolveCurrentEquipmentStatus(Long equipmentId) {
        return equipmentStatusLogsRepository
                .findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(equipmentId)
                .or(() -> equipmentStatusLogsRepository.findFirstByEquipmentIdOrderByStartTimeDesc(equipmentId))
                .map(EquipmentStatusLogs::getStatus)
                .orElse(null);
    }

    private boolean isDispatchableRequest(Request request) {
        if (request == null || request.getStatus() == null) {
            return false;
        }

        String status = request.getStatus().trim().toUpperCase();
        return "APPROVED".equals(status) || "ACCEPTED".equals(status) || "PROCESSING".equals(status);
    }
}

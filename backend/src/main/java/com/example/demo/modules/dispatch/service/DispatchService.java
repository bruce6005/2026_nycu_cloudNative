package com.example.demo.modules.dispatch.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.dispatch.dto.CreateWIPBatchRequest;
import com.example.demo.modules.dispatch.dto.EquipmentWithRecipesDTO;
import com.example.demo.modules.dispatch.dto.PendingSamplesGroupedByRequestDTO;
import com.example.demo.modules.dispatch.dto.RecipeDTO;
import com.example.demo.modules.wip.dto.WIPBatchDTO;
import com.example.demo.modules.dispatch.repository.EquipmentRepository;
import com.example.demo.modules.dispatch.repository.EquipmentStatusLogsRepository;
import com.example.demo.modules.dispatch.repository.RecipeRepository;
import com.example.demo.modules.dispatch.repository.SampleRepository;
import com.example.demo.modules.dispatch.repository.WIPbatchRepository;
import com.example.demo.modules.tempdb.model.Equipment;
import com.example.demo.modules.tempdb.model.EquipmentStatusLogs;
import com.example.demo.modules.tempdb.model.Recipe;
import com.example.demo.modules.tempdb.model.Request;
import com.example.demo.modules.tempdb.model.Sample;
import com.example.demo.modules.tempdb.model.WIPbatch;

@Service
public class DispatchService {

    private final SampleRepository sampleRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentStatusLogsRepository equipmentStatusLogsRepository;
    private final RecipeRepository recipeRepository;
    private final WIPbatchRepository wipbatchRepository;

    public DispatchService(SampleRepository sampleRepository,
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
        Map<Long, List<Sample>> groupedSamples = sampleRepository.findByBatchIsNull().stream()
                .filter(sample -> isDispatchableRequest(sample.getRequest()))
                .collect(Collectors.groupingBy(sample -> sample.getRequest().getId(), LinkedHashMap::new, Collectors.toList()));

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

        if (!recipe.getEquipment().getId().equals(equipment.getId())) {
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
        dto.setRecipes(recipeRepository.findByEquipmentId(equipment.getId()).stream()
                .map(this::toRecipeDTO)
                .toList());
        return dto;
    }

    private RecipeDTO toRecipeDTO(Recipe recipe) {
        RecipeDTO dto = new RecipeDTO();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setVersion(recipe.getVersion());
        return dto;
    }

    private WIPBatchDTO toWIPBatchDTO(WIPbatch batch) {
        WIPBatchDTO dto = new WIPBatchDTO();
        dto.setId(batch.getId());
        dto.setRecipeId(batch.getRecipe().getId());
        dto.setRecipeName(batch.getRecipe().getName());
        dto.setEquipmentId(batch.getRecipe().getEquipment().getId());
        dto.setEquipmentName(batch.getRecipe().getEquipment().getName());
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
        return String.valueOf(request.getId());
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
        return "APPROVED".equals(status) || "ACCEPTED".equals(status);
    }
}

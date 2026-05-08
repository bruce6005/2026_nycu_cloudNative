package com.example.demo.modules.history.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.modules.history.dto.HistoryRequestGroupDTO;
import com.example.demo.modules.history.dto.HistorySampleDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.wip_builder.model.WIPbatch;

@Service
public class HistoryService {

    private final SampleRepository sampleRepository;

    public HistoryService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoryRequestGroupDTO> getHistoryGroupedByRequest() {
        Map<Long, List<Sample>> groupedSamples = sampleRepository.findAll().stream()
                .filter(sample -> sample.getRequest() != null)
                .sorted(Comparator.comparing(sample -> sample.getRequest().getCreateTime(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.groupingBy(sample -> sample.getRequest().getId(), LinkedHashMap::new,
                        Collectors.toList()));

        List<HistoryRequestGroupDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Sample>> entry : groupedSamples.entrySet()) {
            List<Sample> samples = entry.getValue();
            Request request = samples.get(0).getRequest();

            HistoryRequestGroupDTO dto = new HistoryRequestGroupDTO();
            dto.setRequestId(request.getId());
            dto.setRequestTitle(request.getTitle());
            dto.setRequestDescription(request.getDescription());
            dto.setRequestStatus(request.getStatus());
            dto.setPriority(request.getPriority());
            dto.setCreateTime(request.getCreateTime());
            dto.setEndTime(request.getEndTime());
            dto.setSampleCount(samples.size());
            dto.setSamples(samples.stream().map(this::toSampleDTO).toList());
            result.add(dto);
        }

        return result;
    }

    private HistorySampleDTO toSampleDTO(Sample sample) {
        HistorySampleDTO dto = new HistorySampleDTO();
        dto.setSampleId(sample.getId());
        dto.setBarcode(sample.getBarcode());
        dto.setStatus(sample.getStatus());

        WIPbatch batch = sample.getBatch();
        if (batch != null) {
            dto.setBatchId(batch.getId());
            dto.setBatchStatus(batch.getStatus());
            dto.setEquipmentName(batch.getEquipment() != null ? batch.getEquipment().getName() : null);
            dto.setRecipeName(batch.getRecipe() != null ? batch.getRecipe().getName() : null);
        }

        return dto;
    }
}

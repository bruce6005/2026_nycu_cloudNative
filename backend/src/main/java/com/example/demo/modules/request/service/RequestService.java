package com.example.demo.modules.request.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.model.UserRole;
import com.example.demo.modules.auth.repository.UserRepository;

import com.example.demo.modules.request.dto.RequestDTO;
import com.example.demo.modules.request.dto.SampleDTO;
import com.example.demo.modules.request.model.Request;
import com.example.demo.modules.request.model.Sample;
import com.example.demo.modules.request.repository.RequestRepository;
import com.example.demo.modules.request.repository.SampleRepository;
import com.example.demo.modules.recipe.repository.RecipeRepository;
import com.example.demo.modules.recipe.model.Recipe;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    /**
     * 建立新的委託單
     */
    @org.springframework.transaction.annotation.Transactional
    public RequestDTO createRequest(RequestDTO dto) {
        System.out.println("[DEBUG] createRequest for user: " + dto.getFactoryUserId());
        
        User factoryUser = userRepository.findById(dto.getFactoryUserId())
                .orElseThrow(() -> new RuntimeException("Factory user not found"));

        Long managerId = factoryUser.getManagerId();
        System.out.println("[DEBUG] Found managerId: " + managerId);

        if (managerId == null) {
            throw new RuntimeException("Factory user has no manager assigned");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        if (manager.getRole() != UserRole.MANAGER && manager.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Assigned manager is not valid");
        }

        Request request = new Request();

        request.setTitle(dto.getTitle());
        request.setFactoryUser(factoryUser);
        request.setApprover(manager);
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        request.setDescription(dto.getDescription());

        request.setStatus("PENDING");
        request.setCreateTime(LocalDateTime.now());

        Request saved = requestRepository.save(request);
        System.out.println("[DEBUG] Saved Request ID: " + saved.getId());

        // 如果有傳入 samples，則建立它們
        if (dto.getSamples() != null && !dto.getSamples().isEmpty()) {
            for (SampleDTO sDto : dto.getSamples()) {
                System.out.println("[DEBUG] Adding sample barcode: " + sDto.getBarcode() + " with recipe: " + sDto.getRecipeId());
                Sample sample = new Sample();
                sample.setRequest(saved);
                sample.setBarcode(sDto.getBarcode());
                sample.setStatus("PENDING");
                
                if (sDto.getRecipeId() != null) {
                    Recipe recipe = recipeRepository.findById(sDto.getRecipeId())
                            .orElseThrow(() -> new RuntimeException("Recipe not found: " + sDto.getRecipeId()));
                    sample.setRecipe(recipe);
                }
                
                sampleRepository.save(sample);
            }
        } else {
            throw new RuntimeException("At least one sample is required");
        }

        return convertToDTO(saved);
    }

    /**
     * 取得所有委託單清單
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<RequestDTO> getAllRequest() {
        return requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得單一委託單詳情
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public RequestDTO getRequestById(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return convertToDTO(request);
    }

    /**
     * 輔助方法：將 Entity 轉為 DTO
     */
    private RequestDTO convertToDTO(Request entity) {
        RequestDTO dto = new RequestDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setStatus(entity.getStatus());
        dto.setFactoryUserId(entity.getFactoryUser() != null ? entity.getFactoryUser().getId() : null);
        dto.setApproverId(entity.getApprover() != null ? entity.getApprover().getId() : null);
        dto.setPriority(entity.getPriority());
        dto.setDescription(entity.getDescription());
        dto.setRejectReason(entity.getRejectReason());

        // 讀取 samples
        List<Sample> samples = sampleRepository.findByRequest_Id(entity.getId());
        List<SampleDTO> sampleDTOs = samples.stream().map(s -> {
            SampleDTO sDto = new SampleDTO();
            sDto.setBarcode(s.getBarcode());
            sDto.setStatus(s.getStatus());
            if (s.getRecipe() != null) {
                sDto.setRecipeId(s.getRecipe().getId());
                sDto.setRecipeName(s.getRecipe().getName());
                sDto.setRecipeParameters(s.getRecipe().getParameters());
            }
            return sDto;
        }).collect(Collectors.toList());
        dto.setSamples(sampleDTOs);

        return dto;
    }
}

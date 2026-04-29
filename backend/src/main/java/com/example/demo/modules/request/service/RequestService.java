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
import com.example.demo.modules.tempdb.model.Request;
import com.example.demo.modules.tempdb.repository.RequestRepository;

@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;


    /**
     * 建立新的委託單
     */
    public RequestDTO createRequest(RequestDTO dto) {
        User factoryUser = userRepository.findById(dto.getFactoryUserId())
                .orElseThrow(() -> new RuntimeException("Factory user not found"));

        Long managerId = factoryUser.getManagerId();

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
        request.setFactoryUser(factoryUser); // 改為設定物件
        request.setApprover(manager);       // 改為設定物件
        request.setPriority(dto.getPriority() != null ? dto.getPriority().toString() : "NORMAL"); // 轉為 String
        request.setDescription(dto.getDescription());

        request.setStatus("PENDING"); // tempdb 中 status 是 String
        request.setCreateTime(LocalDateTime.now());

        Request saved = requestRepository.save(request);

        return convertToDTO(saved);
    }

    /**
     * 取得所有委託單清單
     */
    public List<RequestDTO> getAllRequest() {
        return requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得單一委託單詳情
     */
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
        
        // 嘗試將 String 優先度轉回 Integer 給前端 DTO
        try {
            dto.setPriority(Integer.parseInt(entity.getPriority()));
        } catch (Exception e) {
            dto.setPriority(5); // 預設值
        }
        
        dto.setDescription(entity.getDescription());
        return dto;
    }
}

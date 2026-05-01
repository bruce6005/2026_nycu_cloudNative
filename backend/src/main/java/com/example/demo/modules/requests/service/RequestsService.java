package com.example.demo.modules.requests.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.model.UserRole;
import com.example.demo.modules.auth.repository.UserRepository;

import com.example.demo.modules.requests.dto.RequestsDTO;
import com.example.demo.modules.requests.model.Requests;
import com.example.demo.modules.requests.model.RequestsStatus;
import com.example.demo.modules.requests.repository.RequestsRepository;
import org.springframework.transaction.annotation.Transactional;
@Service
public class RequestsService {

    @Autowired
    private RequestsRepository requestRepository;

    @Autowired
    private UserRepository userRepository;
    public RequestsDTO createRequest(RequestsDTO dto) {
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

        Requests requests = new Requests();

        requests.setTitle(dto.getTitle());
        requests.setFactoryUserId(factoryUser.getId());
        requests.setApproverId(manager.getId());
        requests.setPriority(dto.getPriority());
        requests.setDescription(dto.getDescription());

        requests.setStatus(RequestsStatus.PENDING);
        requests.setCreateTime(LocalDateTime.now());

        Requests saved = requestRepository.save(requests);

        return convertToDTO(saved);
    }

    /**
     * 取得所有委託單清單
     */
    public List<RequestsDTO> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 取得單一委託單詳情
     */
    public RequestsDTO getRequestById(Long id) {
        Requests requests = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return convertToDTO(requests);
    }


    @Transactional
    public RequestsDTO receiveRequest(Long id) {
        Requests request = requestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestsStatus.APPROVED) {
            throw new IllegalStateException(
                "Only approved requests can be received"
            );
        }

        // 2. 存檔時 JPA 會發出 SQL UPDATE requests ... SET status = 'RECEIVED', version = 6 WHERE id = ? AND version = 5
        // 如果這段時間內別人動過這筆資料，version 會不符合，進而拋出異常
        try {
            Requests savedRequest = requestRepository.save(request);
            return convertToDTO(savedRequest);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("該委託單已被其他人處理，請重新整理頁面");
        }
    }

    // 撈REQUEST FULTER BY STATUS
    public List<RequestsDTO> getByStatus(String status) {
        // 1. 先從 DB 撈出 Entity List
        List<Requests> entities = requestRepository.findByStatus(
            RequestsStatus.valueOf(status.toUpperCase())
        );

        // 2. 透過 Stream 轉換成 DTO List
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    /**
     * 輔助方法：將 Entity 轉為 DTO
     */
    private RequestsDTO convertToDTO(Requests entity) {
        RequestsDTO dto = new RequestsDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setStatus(entity.getStatus().name());
        dto.setFactoryUserId(entity.getFactoryUserId());
        dto.setApproverId(entity.getApproverId());
        dto.setPriority(entity.getPriority());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}

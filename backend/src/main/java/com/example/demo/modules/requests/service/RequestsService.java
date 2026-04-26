package com.example.demo.modules.requests.service;

import com.example.demo.modules.requests.dto.RequestsDTO;
import com.example.demo.modules.requests.model.Requests;
import com.example.demo.modules.requests.repository.RequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestsService {

    @Autowired
    private RequestsRepository requestRepository;

    /**
     * 建立新的委託單
     */
    public RequestsDTO createRequest(RequestsDTO dto) {
        Requests requests = new Requests();

        // 將 DTO 的資料搬運到 Entity
        requests.setTitle(dto.getTitle());
        requests.setFactoryUserId(dto.getFactoryUserId());
        requests.setPriority(dto.getPriority());
        requests.setDescription(dto.getDescription());

        // 設定初始狀態與時間
        requests.setStatus("draft");
        requests.setCreateTime(LocalDateTime.now());

        // 存入資料庫
        Requests saved = requestRepository.save(requests);

        // 將結果轉回 DTO 回傳給前端
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

    /**
     * 輔助方法：將 Entity 轉為 DTO
     */
    private RequestsDTO convertToDTO(Requests entity) {
        RequestsDTO dto = new RequestsDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setStatus(entity.getStatus());
        dto.setFactoryUserId(entity.getFactoryUserId());
        dto.setApproverId(entity.getApproverId());
        dto.setPriority(entity.getPriority());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}

package com.example.demo.modules.request;

import com.example.demo.modules.request.controller.RequestController;
import com.example.demo.modules.request.dto.RequestDTO;
import com.example.demo.modules.request.service.RequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RequestController}.
 *
 * 測試範疇：
 *  - POST /api/request 委派給 service.createRequest()
 *  - GET  /api/request 委派給 service.getAllRequest()
 *  - GET  /api/request/{id} 委派給 service.getRequestById()
 *  - PATCH /api/request/{id}/archive 委派給 service.archiveRequest()
 */
@ExtendWith(MockitoExtension.class)
class RequestControllerTest {

    @Mock
    private RequestService requestService;

    @InjectMocks
    private RequestController requestController;

    // -------------------------------------------------------
    // createRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("createRequest() 應委派給 service 並回傳結果")
    void createRequest_shouldDelegateToService() {
        RequestDTO inputDto = new RequestDTO();
        RequestDTO expectedDto = new RequestDTO();
        expectedDto.setId(1L);

        when(requestService.createRequest(inputDto)).thenReturn(expectedDto);

        RequestDTO result = requestController.createRequest(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(requestService, times(1)).createRequest(inputDto);
    }

    // -------------------------------------------------------
    // getAllRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("getAllRequest() 應回傳 service 提供的列表")
    void getAllRequest_shouldReturnList() {
        RequestDTO dto = new RequestDTO();
        dto.setId(1L);

        when(requestService.getAllRequest()).thenReturn(List.of(dto));

        List<RequestDTO> result = requestController.getAllRequest();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(requestService, times(1)).getAllRequest();
    }

    // -------------------------------------------------------
    // getRequestById()
    // -------------------------------------------------------

    @Test
    @DisplayName("getRequestById() 應委派給 service 並回傳對應 DTO")
    void getRequestById_shouldReturnDTO() {
        RequestDTO dto = new RequestDTO();
        dto.setId(5L);

        when(requestService.getRequestById(5L)).thenReturn(dto);

        RequestDTO result = requestController.getRequestById(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        verify(requestService, times(1)).getRequestById(5L);
    }

    // -------------------------------------------------------
    // archiveRequest()
    // -------------------------------------------------------

    @Test
    @DisplayName("archiveRequest() 應呼叫 service 並回傳 204 No Content")
    void archiveRequest_shouldReturn204() {
        doNothing().when(requestService).archiveRequest(1L);

        ResponseEntity<Void> response = requestController.archiveRequest(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(requestService, times(1)).archiveRequest(1L);
    }
}

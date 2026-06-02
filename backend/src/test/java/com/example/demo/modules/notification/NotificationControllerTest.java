package com.example.demo.modules.notification;

import com.example.demo.modules.notification.controller.NotificationController;
import com.example.demo.modules.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationController}.
 *
 * 測試範疇：
 *  - GET /api/sse/subscribe 正確委派給 NotificationService
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    // -------------------------------------------------------
    // subscribe()
    // -------------------------------------------------------

    @Test
    @DisplayName("subscribe() 應呼叫 notificationService.subscribe() 並回傳其結果")
    void subscribe_shouldDelegateToService() {
        // Given
        SseEmitter mockEmitter = mock(SseEmitter.class);
        when(notificationService.subscribe()).thenReturn(mockEmitter);

        // When
        SseEmitter result = notificationController.subscribe();

        // Then
        assertSame(mockEmitter, result, "controlller 應直接回傳 service 所給的 SseEmitter");
        verify(notificationService, times(1)).subscribe();
    }

    @Test
    @DisplayName("subscribe() 回傳值不應為 null（當 service 回傳有效 emitter）")
    void subscribe_shouldNotReturnNull() {
        // Given
        when(notificationService.subscribe()).thenReturn(mock(SseEmitter.class));

        // When
        SseEmitter result = notificationController.subscribe();

        // Then
        assertNotNull(result);
    }
}

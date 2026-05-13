package com.example.demo.modules.notification;

import com.example.demo.modules.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NotificationService}.
 *
 * 測試範疇：
 *  - subscribe() 正常回傳 SseEmitter
 *  - broadcast() 正常廣播事件
 *  - sendHeartbeat() 觸發 keep-alive 廣播
 */
class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    // -------------------------------------------------------
    // subscribe()
    // -------------------------------------------------------

    @Test
    @DisplayName("subscribe() 應回傳非 null 的 SseEmitter")
    void subscribe_shouldReturnEmitter() {
        SseEmitter emitter = notificationService.subscribe();
        assertNotNull(emitter, "subscribe() 回傳的 SseEmitter 不應為 null");
    }

    @Test
    @DisplayName("多次 subscribe() 應各自回傳獨立的 SseEmitter 實例")
    void subscribe_shouldReturnDistinctEmitters() {
        SseEmitter emitter1 = notificationService.subscribe();
        SseEmitter emitter2 = notificationService.subscribe();
        assertNotSame(emitter1, emitter2, "每次 subscribe() 應回傳不同的 SseEmitter 實例");
    }

    // -------------------------------------------------------
    // broadcast()
    // -------------------------------------------------------

    @Test
    @DisplayName("broadcast() 在沒有訂閱者時不應拋出例外")
    void broadcast_withNoSubscribers_shouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.broadcast("TEST_EVENT", "payload"),
                "沒有訂閱者時 broadcast() 不應拋出例外");
    }

    @Test
    @DisplayName("broadcast() 在有訂閱者後呼叫不應拋出例外")
    void broadcast_withSubscribers_shouldNotThrow() {
        // Given: 有一個已訂閱的 client
        notificationService.subscribe();

        // When & Then
        assertDoesNotThrow(() -> notificationService.broadcast("REQUEST_UPDATED", "new request"),
                "有訂閱者時 broadcast() 不應拋出例外");
    }

    // -------------------------------------------------------
    // sendHeartbeat()
    // -------------------------------------------------------

    @Test
    @DisplayName("sendHeartbeat() 不應拋出例外")
    void sendHeartbeat_shouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.sendHeartbeat(),
                "sendHeartbeat() 不應拋出例外");
    }
}

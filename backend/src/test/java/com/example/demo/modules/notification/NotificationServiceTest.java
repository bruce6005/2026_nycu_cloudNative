package com.example.demo.modules.notification;

import com.example.demo.modules.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NotificationService}.
 * 
 * ----------------------------------------------------
 * 測試功能清單 (Test Coverage Summary):
 * ----------------------------------------------------
 * 1. 連線管理 (Subscribe):
 *    - [x] 正常訂閱：應回傳獨立的 SseEmitter 實例
 * 
 * 2. 廣播機制 (Broadcast):
 *    - [x] 正常廣播：無訂閱或有訂閱時皆不應拋出例外
 *    - [x] 異常清理：當連線失效(IOException)時，應自動移除該連線 -> [核心穩定性測試]
 * 
 * 3. 心跳包 (Heartbeat):
 *    - [x] 週期廣播：驗證 heartbeat 觸發
 * ----------------------------------------------------
 */
@SuppressWarnings("unchecked")
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

    // -------------------------------------------------------
    // 穩定性測試 (Stability Tests)
    // -------------------------------------------------------

    @Test
    @DisplayName("當 SseEmitter 發送失敗 (IOException) 時，應自動將其從清單中移除")
    void broadcast_shouldRemoveEmitterOnIOException() throws IOException {
        // Given: 手動注入一個 Mock 且會失敗的 SseEmitter
        SseEmitter mockEmitter = Mockito.mock(SseEmitter.class);
        Mockito.doThrow(new IOException("Connection broken")).when(mockEmitter).send(Mockito.any(SseEmitter.SseEventBuilder.class));
        
        // 透過 Reflection 拿到 private 的 emitters List 並加入 mock
        List<SseEmitter> internalList = (List<SseEmitter>) ReflectionTestUtils.getField(notificationService, "emitters");
        internalList.add(mockEmitter);
        assertEquals(1, internalList.size(), "清單一開始應有一個連線");

        // When: 執行廣播
        notificationService.broadcast("FAIL_EVENT", "error");

        // Then: 連線應被移除
        assertTrue(internalList.isEmpty(), "發生 IOException 後，emitters 清單應被清空");
    }
}

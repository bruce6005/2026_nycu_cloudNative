import { useEffect } from 'react';
import { CONFIG } from '../../config/config';

/**
 * 自訂 SSE 監聽 Hook
 * @param eventName 要監聽的事件名稱 (例如 "REQUEST_UPDATED")
 * @param callback 收到事件時要執行的動作 (例如 loadData)
 */
export const useSse = (eventName: string, callback: () => void) => {
  useEffect(() => {
    // 建立連線
    const eventSource = new EventSource(`${CONFIG.API_BASE}/api/sse/subscribe`);

    // 監聽特定事件
    eventSource.addEventListener(eventName, (event) => {
      console.log(`[SSE] Received ${eventName}:`, event.data);
      callback();
    });

    // 錯誤處理與重連
    eventSource.onerror = (err) => {
      console.error('[SSE] Connection error:', err);
      // EventSource 預設會自動重連，這裡可以加一些額外的 log 或邏輯
    };

    // 元件卸載時斷開連線
    return () => {
      eventSource.close();
      console.log('[SSE] Connection closed');
    };
  }, [eventName, callback]);
};

package com.example.demo.modules.notification.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    // 每 20 秒發送一個心跳包，維持連線活躍並確保即時性
    @Scheduled(fixedRate = 20000)
    public void sendHeartbeat() {
        broadcast("HEARTBEAT", "keep-alive");
    }

    // 儲存所有在線上的連線
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // 設定 30 分鐘超時
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        this.emitters.add(emitter);

        // 連線結束、超時或錯誤時移除
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        // 發送一個初始連線成功的信號
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    // 發送信號給所有人
    public void broadcast(String eventName, String data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                this.emitters.remove(emitter);
            }
        }
    }
}

package com.example.demo.modules.equipment.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipment_alarms")
public class EquipmentAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK: equipment_id
     * 先用 Long 對接欄位，避免 Equipment entity 尚未完成時卡住。
     */
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    /**
     * FK: handler_id
     * 對應處理告警的 user id。
     */
    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "error_code", nullable = false)
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (isResolved == null) {
            isResolved = false;
        }
    }
}
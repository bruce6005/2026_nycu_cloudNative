package com.example.demo.modules.tempdb.model;

import com.example.demo.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "equipments_alarms")
public class EquipmentsAlarms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private User handler;

    @Column(name = "error_code", nullable = false)
    private String errorCode;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "solve_time")
    private LocalDateTime solveTime;

    @Column(name = "is_resolved", nullable = false, columnDefinition = "boolean default false")
    private Boolean isResolved = false;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;
}

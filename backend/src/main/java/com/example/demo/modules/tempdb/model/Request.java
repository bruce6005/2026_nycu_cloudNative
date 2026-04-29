package com.example.demo.modules.tempdb.model;

import java.time.LocalDateTime;

import com.example.demo.modules.auth.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "request")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_user_id", nullable = false)
    private User factoryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(nullable = false)
    private String priority = "NORMAL";

    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "draft_content", columnDefinition = "json")
    private String draftContent;

    @Column(columnDefinition = "TEXT")
    private String description;
}

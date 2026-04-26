package com.example.demo.modules.requests.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Requests")
@Getter
@Setter
@NoArgsConstructor
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long factoryUserId;

    private Long approverId;

    private String title;

    private Integer priority;

    @Enumerated(EnumType.STRING)
    private RequestsStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createTime;

    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String draftContent;
}
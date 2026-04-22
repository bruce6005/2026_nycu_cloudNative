// 還沒MERGE 我先mock一個 之後再合併
package com.example.demo.modules.requests.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;

    private Long factoryUserId;
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('DRAFT','SUBMITTED','APPROVED','REJECTED','PENDING')")
    private RequestsStatus status;
    private String description;

    private LocalDateTime createTime;
    private LocalDateTime endTime;
}
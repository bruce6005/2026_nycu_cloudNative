package com.example.demo.modules.requests.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Requests")
public class Requests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long factoryUserId; // 對應 (*fk)factory_user_id
    private Long approverId; // 對應 (*fk)approver_id
    private String title; // 名稱
    private Integer priority; // 優先度
    private String status; // 狀態

    @Column(columnDefinition = "TEXT")
    private String description; // 描述

    private LocalDateTime createTime; // 建立時間
    private LocalDateTime endTime; // 結束時間

    @Column(columnDefinition = "TEXT")
    private String draftContent; // 存放 JSON 的內容

    public Requests() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFactoryUserId() {
        return factoryUserId;
    }

    public void setFactoryUserId(Long factoryUserId) {
        this.factoryUserId = factoryUserId;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getDraftContent() {
        return draftContent;
    }

    public void setDraftContent(String draftContent) {
        this.draftContent = draftContent;
    }
}
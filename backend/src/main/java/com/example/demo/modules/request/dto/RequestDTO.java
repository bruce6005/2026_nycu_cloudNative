package com.example.demo.modules.request.dto;

public class RequestDTO {
    private Long id;
    private String title;
    private String status;

    private Long factoryUserId;
    private Long approverId;
    private String priority;
    private String description;
    private java.util.List<SampleDTO> samples;

    // 前端建立時需要的建構子
    public RequestDTO() {
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public java.util.List<SampleDTO> getSamples() {
        return samples;
    }

    public void setSamples(java.util.List<SampleDTO> samples) {
        this.samples = samples;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

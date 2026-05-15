package com.parttime.cservice.core.model;

import java.time.LocalDateTime;

public class WorkerResume {

    private Long id;
    private Long workerId;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;

    public WorkerResume() {}

    public WorkerResume(Long id, Long workerId, String fileName, String fileUrl, LocalDateTime uploadedAt) {
        this.id = id;
        this.workerId = workerId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}

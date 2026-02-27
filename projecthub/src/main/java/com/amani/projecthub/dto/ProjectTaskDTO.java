package com.amani.projecthub.dto;

import java.time.LocalDateTime;

public class ProjectTaskDTO {

    private Long id;
    private String title;
    private boolean completed;
    private LocalDateTime createdAt;
    private Long stageId;
    private String stageName;
    private Long projectId;

    public ProjectTaskDTO() {}

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public boolean isCompleted() { return completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Long getStageId() { return stageId; }

    public String getStageName() { return stageName; }

    public Long getProjectId() { return projectId; }

    // ── Setters ──────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setStageId(Long stageId) { this.stageId = stageId; }

    public void setStageName(String stageName) { this.stageName = stageName; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
package com.amani.projecthub.dto;

import java.time.LocalDateTime;

public class NoteDTO {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long stageId;
    private String stageName;
    private Long projectId;

    public NoteDTO() {}

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getContent() { return content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Long getStageId() { return stageId; }

    public String getStageName() { return stageName; }

    public Long getProjectId() { return projectId; }

    // ── Setters ──────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setContent(String content) { this.content = content; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setStageId(Long stageId) { this.stageId = stageId; }

    public void setStageName(String stageName) { this.stageName = stageName; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
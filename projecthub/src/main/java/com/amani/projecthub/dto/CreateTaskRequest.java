package com.amani.projecthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Stage ID is required")
    private Long stageId;

    public CreateTaskRequest() {}

    // ── Getters ──────────────────────────────────────────

    public String getTitle() { return title; }

    public Long getStageId() { return stageId; }

    // ── Setters ──────────────────────────────────────────

    public void setTitle(String title) { this.title = title; }

    public void setStageId(Long stageId) { this.stageId = stageId; }
}
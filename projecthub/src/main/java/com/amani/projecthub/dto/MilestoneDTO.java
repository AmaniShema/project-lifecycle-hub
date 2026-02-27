package com.amani.projecthub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MilestoneDTO {

    private Long id;
    private String title;
    private LocalDate dueDate;
    private boolean completed;
    private LocalDateTime createdAt;
    private Long projectId;

    // Computed fields — never stored
    private boolean overdue;
    private long daysUntilDue;

    public MilestoneDTO() {}

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public LocalDate getDueDate() { return dueDate; }

    public boolean isCompleted() { return completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Long getProjectId() { return projectId; }

    public boolean isOverdue() { return overdue; }

    public long getDaysUntilDue() { return daysUntilDue; }

    // ── Setters ──────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public void setDaysUntilDue(long daysUntilDue) { this.daysUntilDue = daysUntilDue; }
}
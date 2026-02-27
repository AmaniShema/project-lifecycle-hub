package com.amani.projecthub.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private String currentStage;
    private LocalDateTime createdAt;

    // Dynamically calculated — never stored
    private double progress;

    // Task summary — never stored
    private long totalTasks;
    private long completedTasks;

    // Milestone summary — never stored
    private int totalMilestones;
    private int overdueMilestones;
    private String upcomingMilestoneTitle;
    private LocalDate upcomingMilestoneDueDate;
    private long upcomingMilestoneDaysLeft;

    private List<StageDTO> stages;

    public ProjectDTO() {}

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getCurrentStage() { return currentStage; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public double getProgress() { return progress; }

    public long getTotalTasks() { return totalTasks; }

    public long getCompletedTasks() { return completedTasks; }

    public int getTotalMilestones() { return totalMilestones; }

    public int getOverdueMilestones() { return overdueMilestones; }

    public String getUpcomingMilestoneTitle() { return upcomingMilestoneTitle; }

    public LocalDate getUpcomingMilestoneDueDate() { return upcomingMilestoneDueDate; }

    public long getUpcomingMilestoneDaysLeft() { return upcomingMilestoneDaysLeft; }

    public List<StageDTO> getStages() { return stages; }

    // ── Setters ──────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setProgress(double progress) { this.progress = progress; }

    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }

    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }

    public void setTotalMilestones(int totalMilestones) { this.totalMilestones = totalMilestones; }

    public void setOverdueMilestones(int overdueMilestones) { this.overdueMilestones = overdueMilestones; }

    public void setUpcomingMilestoneTitle(String upcomingMilestoneTitle) { this.upcomingMilestoneTitle = upcomingMilestoneTitle; }

    public void setUpcomingMilestoneDueDate(LocalDate upcomingMilestoneDueDate) { this.upcomingMilestoneDueDate = upcomingMilestoneDueDate; }

    public void setUpcomingMilestoneDaysLeft(long upcomingMilestoneDaysLeft) { this.upcomingMilestoneDaysLeft = upcomingMilestoneDaysLeft; }

    public void setStages(List<StageDTO> stages) { this.stages = stages; }
}
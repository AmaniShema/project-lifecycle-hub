package com.amani.projecthub.dto;

import java.util.List;

public class StageDTO {

    private Long id;
    private String name;
    private Long projectId;
    private List<ProjectTaskDTO> tasks;

    public StageDTO() {}

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public Long getProjectId() { return projectId; }

    public List<ProjectTaskDTO> getTasks() { return tasks; }

    // ── Setters ──────────────────────────────────────────

    public void setId(Long id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public void setTasks(List<ProjectTaskDTO> tasks) { this.tasks = tasks; }
}
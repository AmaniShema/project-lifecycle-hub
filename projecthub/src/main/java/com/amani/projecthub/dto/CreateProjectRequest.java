package com.amani.projecthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public CreateProjectRequest() {}

    // ── Getters ──────────────────────────────────────────

    public String getName() { return name; }

    public String getDescription() { return description; }

    // ── Setters ──────────────────────────────────────────

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }
}
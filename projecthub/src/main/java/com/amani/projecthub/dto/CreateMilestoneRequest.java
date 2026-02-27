package com.amani.projecthub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateMilestoneRequest {

    @NotBlank(message = "Milestone title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    public CreateMilestoneRequest() {}

    // ── Getters ──────────────────────────────────────────

    public String getTitle() { return title; }

    public LocalDate getDueDate() { return dueDate; }

    // ── Setters ──────────────────────────────────────────

    public void setTitle(String title) { this.title = title; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
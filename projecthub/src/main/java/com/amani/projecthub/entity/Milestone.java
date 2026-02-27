package com.amani.projecthub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milestones")
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Milestone title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @NotNull(message = "Due date is required")
    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Milestone() {}

    public Milestone(String title, LocalDate dueDate, Project project) {
        this.title = title;
        this.dueDate = dueDate;
        this.project = project;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public LocalDate getDueDate() { return dueDate; }

    public boolean isCompleted() { return completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Project getProject() { return project; }

    // ── Setters ──────────────────────────────────────────

    public void setTitle(String title) { this.title = title; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setProject(Project project) { this.project = project; }
}
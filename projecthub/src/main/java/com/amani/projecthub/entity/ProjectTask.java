package com.amani.projecthub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public ProjectTask() {}

    public ProjectTask(String title, Stage stage, Project project) {
        this.title = title;
        this.stage = stage;
        this.project = project;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public boolean isCompleted() { return completed; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Stage getStage() { return stage; }

    public Project getProject() { return project; }

    // ── Setters ──────────────────────────────────────────

    public void setTitle(String title) { this.title = title; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setStage(Stage stage) { this.stage = stage; }

    public void setProject(Project project) { this.project = project; }
}
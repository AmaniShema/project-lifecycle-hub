package com.amani.projecthub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Note content is required")
    @Size(max = 2000, message = "Note must not exceed 2000 characters")
    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Note() {}

    public Note(String content, Stage stage, Project project) {
        this.content = content;
        this.stage = stage;
        this.project = project;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getContent() { return content; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Stage getStage() { return stage; }

    public Project getProject() { return project; }

    // ── Setters ──────────────────────────────────────────

    public void setContent(String content) { this.content = content; }

    public void setStage(Stage stage) { this.stage = stage; }

    public void setProject(Project project) { this.project = project; }
}
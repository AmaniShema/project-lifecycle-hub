package com.amani.projecthub.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.amani.projecthub.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String currentStage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    public Project() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.currentStage = "IDEA";
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getCurrentStage() { return currentStage; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<Stage> getStages() { return stages; }

    public List<ProjectTask> getTasks() { return tasks; }

    // ── Setters ──────────────────────────────────────────

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public void setStages(List<Stage> stages) { this.stages = stages; }

    public void setTasks(List<ProjectTask> tasks) { this.tasks = tasks; }

    public List<Milestone> getMilestones() { return milestones; }

    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }

    public List<Note> getNotes() { return notes; }

    public void setNotes(List<Note> notes) { this.notes = notes; }

    public User getOwner() { return owner; }

    public void setOwner(User owner) { this.owner = owner; }
}
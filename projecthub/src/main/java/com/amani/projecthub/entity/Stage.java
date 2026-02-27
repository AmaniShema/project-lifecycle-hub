package com.amani.projecthub.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTask> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    public Stage() {}

    public Stage(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getName() { return name; }

    public Project getProject() { return project; }

    public List<ProjectTask> getTasks() { return tasks; }

    // ── Setters ──────────────────────────────────────────

    public void setName(String name) { this.name = name; }

    public void setProject(Project project) { this.project = project; }

    public void setTasks(List<ProjectTask> tasks) { this.tasks = tasks; }

    public List<Note> getNotes() { return notes; }

    public void setNotes(List<Note> notes) { this.notes = notes; }
}
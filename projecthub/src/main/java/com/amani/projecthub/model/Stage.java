package com.amani.projecthub.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stages")
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // IDEA, PLANNING, BUILDING, TESTING, LAUNCH, MAINTENANCE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Stage() {}

    public Stage(String name, Project project) {
        this.name = name;
        this.project = project;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
}

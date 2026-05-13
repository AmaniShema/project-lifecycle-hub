package com.lifecyclehub.entity;

import java.time.LocalDateTime;

/**
 * Entity representing a Project in the Life-Cycle Hub.
 * v1.3.0 — added 'archived' field.
 */
public class Project {

    private int           id;
    private String        name;
    private String        description;
    private String        notes;
    private LocalDateTime createdAt;
    private String        currentStage;
    private boolean       archived;

    // ── Constructors ──

    public Project() {}

    public Project(int id, String name, String description, String notes,
                   LocalDateTime createdAt, String currentStage,
                   boolean archived) {
        this.id           = id;
        this.name         = name;
        this.description  = description;
        this.notes        = notes == null ? "" : notes;
        this.createdAt    = createdAt;
        this.currentStage = currentStage;
        this.archived     = archived;
    }

    public Project(String name, String description,
                   LocalDateTime createdAt, String currentStage) {
        this.name         = name;
        this.description  = description;
        this.notes        = "";
        this.createdAt    = createdAt;
        this.currentStage = currentStage;
        this.archived     = false;
    }

    // ── Getters and Setters ──

    public int           getId()                      { return id; }
    public void          setId(int id)                { this.id = id; }

    public String        getName()                    { return name; }
    public void          setName(String name)         { this.name = name; }

    public String        getDescription()             { return description; }
    public void          setDescription(String d)     { this.description = d; }

    public String        getNotes()                   { return notes == null ? "" : notes; }
    public void          setNotes(String notes)       { this.notes = notes == null ? "" : notes; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void          setCreatedAt(LocalDateTime d){ this.createdAt = d; }

    public String        getCurrentStage()            { return currentStage; }
    public void          setCurrentStage(String s)    { this.currentStage = s; }

    public boolean       isArchived()                 { return archived; }
    public void          setArchived(boolean archived){ this.archived = archived; }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name
                + "', stage='" + currentStage
                + "', archived=" + archived + "}";
    }
}
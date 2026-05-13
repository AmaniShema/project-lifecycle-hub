package com.lifecyclehub.entity;

/**
 * Entity representing a Life-Cycle Stage.
 * v1.1.0 — added notes field.
 */
public class Stage {

    public static final String IDEA        = "IDEA";
    public static final String PLANNING    = "PLANNING";
    public static final String BUILDING    = "BUILDING";
    public static final String TESTING     = "TESTING";
    public static final String LAUNCH      = "LAUNCH";
    public static final String MAINTENANCE = "MAINTENANCE";

    public static final String[] ALL_STAGES = {
            IDEA, PLANNING, BUILDING, TESTING, LAUNCH, MAINTENANCE
    };

    // ─────────────────────────────────────────
    // Fields
    // ─────────────────────────────────────────

    private int    id;
    private String name;
    private int    projectId;
    private String notes;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public Stage() {}

    public Stage(int id, String name, int projectId, String notes) {
        this.id        = id;
        this.name      = name;
        this.projectId = projectId;
        this.notes     = notes == null ? "" : notes;
    }

    public Stage(String name, int projectId) {
        this.name      = name;
        this.projectId = projectId;
        this.notes     = "";
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public int    getId()                     { return id; }
    public void   setId(int id)               { this.id = id; }

    public String getName()                   { return name; }
    public void   setName(String name)        { this.name = name; }

    public int    getProjectId()              { return projectId; }
    public void   setProjectId(int pid)       { this.projectId = pid; }

    public String getNotes()                  { return notes == null ? "" : notes; }
    public void   setNotes(String notes)      { this.notes = notes == null ? "" : notes; }

    @Override
    public String toString() {
        return "Stage{id=" + id + ", name='" + name
                + "', projectId=" + projectId + "}";
    }
}
package com.lifecyclehub.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a Task within a Stage.
 * v2.2.0 — added task-level notes field.
 */
public class Task {

    // ── Priority Enum ─────────────────────────────────────────

    public enum Priority {
        HIGH, MEDIUM, LOW, NONE;

        public String label() {
            return switch (this) {
                case HIGH   -> "! HIGH";
                case MEDIUM -> "~ MED";
                case LOW    -> "▽ LOW";
                case NONE   -> "";
            };
        }

        public static Priority fromString(String s) {
            if (s == null) return NONE;
            try { return valueOf(s.toUpperCase()); }
            catch (IllegalArgumentException e) { return NONE; }
        }
    }

    // ── Fields ────────────────────────────────────────────────

    private int           id;
    private String        title;
    private boolean       completed;
    private LocalDateTime createdAt;
    private int           stageId;
    private int           projectId;
    private LocalDate     dueDate;
    private Priority      priority;
    private String        notes;      // task-specific notes — v2.2.0

    // ── Constructors ──────────────────────────────────────────

    public Task() {}

    /** Full constructor — used when loading from database. */
    public Task(int id, String title, boolean completed,
                LocalDateTime createdAt, int stageId,
                int projectId, LocalDate dueDate,
                Priority priority, String notes) {
        this.id        = id;
        this.title     = title;
        this.completed = completed;
        this.createdAt = createdAt;
        this.stageId   = stageId;
        this.projectId = projectId;
        this.dueDate   = dueDate;
        this.priority  = priority == null ? Priority.NONE : priority;
        this.notes     = notes == null ? "" : notes;
    }

    /** Constructor without ID — used before database insertion. */
    public Task(String title, boolean completed,
                LocalDateTime createdAt, int stageId,
                int projectId, LocalDate dueDate,
                Priority priority, String notes) {
        this.title     = title;
        this.completed = completed;
        this.createdAt = createdAt;
        this.stageId   = stageId;
        this.projectId = projectId;
        this.dueDate   = dueDate;
        this.priority  = priority == null ? Priority.NONE : priority;
        this.notes     = notes == null ? "" : notes;
    }

    /** Backward-compatible constructor without notes. */
    public Task(String title, boolean completed,
                LocalDateTime createdAt, int stageId,
                int projectId, LocalDate dueDate, Priority priority) {
        this(title, completed, createdAt, stageId, projectId, dueDate, priority, "");
    }

    /** Backward-compatible full constructor without notes. */
    public Task(int id, String title, boolean completed,
                LocalDateTime createdAt, int stageId,
                int projectId, LocalDate dueDate, Priority priority) {
        this(id, title, completed, createdAt, stageId, projectId, dueDate, priority, "");
    }

    // ── Due Date Helpers ──────────────────────────────────────

    public boolean isOverdue() {
        if (completed || dueDate == null) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    public boolean isDueSoon() {
        if (completed || dueDate == null) return false;
        LocalDate today = LocalDate.now();
        return !today.isAfter(dueDate) && dueDate.isBefore(today.plusDays(4));
    }

    public boolean hasNotes() {
        return notes != null && !notes.isBlank();
    }

    // ── Getters and Setters ───────────────────────────────────

    public int           getId()                        { return id; }
    public void          setId(int id)                  { this.id = id; }

    public String        getTitle()                     { return title; }
    public void          setTitle(String t)             { this.title = t; }

    public boolean       isCompleted()                  { return completed; }
    public void          setCompleted(boolean c)        { this.completed = c; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime d)  { this.createdAt = d; }

    public int           getStageId()                   { return stageId; }
    public void          setStageId(int s)              { this.stageId = s; }

    public int           getProjectId()                 { return projectId; }
    public void          setProjectId(int p)            { this.projectId = p; }

    public LocalDate     getDueDate()                   { return dueDate; }
    public void          setDueDate(LocalDate d)        { this.dueDate = d; }

    public Priority      getPriority()                  { return priority == null ? Priority.NONE : priority; }
    public void          setPriority(Priority p)        { this.priority = p == null ? Priority.NONE : p; }

    public String        getNotes()                     { return notes == null ? "" : notes; }
    public void          setNotes(String n)             { this.notes = n == null ? "" : n; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title
            + "', completed=" + completed
            + ", priority=" + priority
            + ", dueDate=" + dueDate + "}";
    }
}

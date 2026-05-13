package com.lifecyclehub.service;

import com.lifecyclehub.entity.Stage;
import com.lifecyclehub.entity.Task;
import com.lifecyclehub.repository.StageRepository;
import com.lifecyclehub.repository.TaskRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TaskService — business logic for Task operations.
 * v2.2.0 — added setTaskNotes().
 */
public class TaskService {

    private final TaskRepository  taskRepository;
    private final StageRepository stageRepository;

    public TaskService() {
        this.taskRepository  = new TaskRepository();
        this.stageRepository = new StageRepository();
    }

    // ── ADD TASK ──────────────────────────────────────────────

    public Task addTask(String title, String stageName,
                        int projectId, LocalDate dueDate,
                        Task.Priority priority) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Task title cannot be empty.");

        Stage stage = stageRepository.findByNameAndProjectId(stageName, projectId);
        if (stage == null)
            throw new RuntimeException("Stage '" + stageName
                + "' not found for project id=" + projectId);

        Task task = new Task(
            title.trim(), false, LocalDateTime.now(),
            stage.getId(), projectId, dueDate,
            priority == null ? Task.Priority.NONE : priority,
            ""
        );
        taskRepository.insert(task);
        System.out.println("[TaskService] Added task '" + title
            + "' to stage " + stageName + " priority: " + task.getPriority());
        return task;
    }

    public Task addTask(String title, String stageName,
                        int projectId, LocalDate dueDate) {
        return addTask(title, stageName, projectId, dueDate, Task.Priority.NONE);
    }

    public Task addTask(String title, String stageName, int projectId) {
        return addTask(title, stageName, projectId, null, Task.Priority.NONE);
    }

    // ── SET DUE DATE ──────────────────────────────────────────

    public void setDueDate(int taskId, LocalDate dueDate) {
        taskRepository.updateDueDate(taskId, dueDate);
    }

    // ── SET PRIORITY ──────────────────────────────────────────

    public void setPriority(int taskId, Task.Priority priority) {
        taskRepository.updatePriority(taskId,
            priority == null ? Task.Priority.NONE : priority);
    }

    // ── SET TASK NOTES ────────────────────────────────────────

    /**
     * Saves task-specific notes (not stage notes).
     * Called when the user clicks "Save" in the task notes panel.
     *
     * @param taskId the task to update
     * @param notes  the notes text (can be empty)
     */
    public void setTaskNotes(int taskId, String notes) {
        taskRepository.updateNotes(taskId, notes == null ? "" : notes);
        System.out.println("[TaskService] Notes saved for task id=" + taskId);
    }

    // ── TOGGLE ────────────────────────────────────────────────

    public void toggleTask(Task task) {
        boolean newStatus = !task.isCompleted();
        taskRepository.updateCompleted(task.getId(), newStatus);
        task.setCompleted(newStatus);
    }

    // ── DELETE ────────────────────────────────────────────────

    public void deleteTask(int taskId) {
        taskRepository.delete(taskId);
    }

    // ── FETCH ─────────────────────────────────────────────────

    public List<Task> getTasksByStage(int stageId) {
        return taskRepository.findByStageId(stageId);
    }

    public List<Task> getTasksByProject(int projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}

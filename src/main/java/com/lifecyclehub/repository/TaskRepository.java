package com.lifecyclehub.repository;

import com.lifecyclehub.database.DatabaseConnection;
import com.lifecyclehub.entity.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskRepository — all SQL for the tasks table.
 * v2.2.0 — includes notes column.
 */
public class TaskRepository {

    private static final String INSERT =
        "INSERT INTO tasks " +
        "(title, completed, created_at, stage_id, project_id, due_date, priority, notes) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_STAGE =
        "SELECT id, title, completed, created_at, " +
        "stage_id, project_id, due_date, priority, notes " +
        "FROM tasks WHERE stage_id = ? ORDER BY created_at ASC";

    private static final String FIND_BY_PROJECT =
        "SELECT id, title, completed, created_at, " +
        "stage_id, project_id, due_date, priority, notes " +
        "FROM tasks WHERE project_id = ? ORDER BY stage_id ASC, created_at ASC";

    private static final String UPDATE_COMPLETED =
        "UPDATE tasks SET completed = ? WHERE id = ?";

    private static final String UPDATE_DUE_DATE =
        "UPDATE tasks SET due_date = ? WHERE id = ?";

    private static final String UPDATE_PRIORITY =
        "UPDATE tasks SET priority = ? WHERE id = ?";

    private static final String UPDATE_NOTES =
        "UPDATE tasks SET notes = ? WHERE id = ?";

    private static final String DELETE =
        "DELETE FROM tasks WHERE id = ?";

    private static final String COUNT_TOTAL =
        "SELECT COUNT(*) FROM tasks WHERE project_id = ?";

    private static final String COUNT_COMPLETED =
        "SELECT COUNT(*) FROM tasks WHERE project_id = ? AND completed = 1";

    // ── INSERT ────────────────────────────────────────────────

    public int insert(Task task) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, task.getTitle());
            stmt.setInt(2, task.isCompleted() ? 1 : 0);
            stmt.setString(3, task.getCreatedAt().toString());
            stmt.setInt(4, task.getStageId());
            stmt.setInt(5, task.getProjectId());
            stmt.setString(6, task.getDueDate() != null ? task.getDueDate().toString() : null);
            stmt.setString(7, task.getPriority().name());
            stmt.setString(8, task.getNotes());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    task.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert task", e);
        }
        return -1;
    }

    // ── FIND BY STAGE ─────────────────────────────────────────

    public List<Task> findByStageId(int stageId) {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_STAGE)) {
            stmt.setInt(1, stageId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tasks by stage", e);
        }
        return tasks;
    }

    // ── FIND BY PROJECT ───────────────────────────────────────

    public List<Task> findByProjectId(int projectId) {
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_PROJECT)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tasks by project", e);
        }
        return tasks;
    }

    // ── UPDATE COMPLETED ──────────────────────────────────────

    public void updateCompleted(int taskId, boolean completed) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_COMPLETED)) {
            stmt.setInt(1, completed ? 1 : 0);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task completion", e);
        }
    }

    // ── UPDATE DUE DATE ───────────────────────────────────────

    public void updateDueDate(int taskId, LocalDate dueDate) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_DUE_DATE)) {
            stmt.setString(1, dueDate != null ? dueDate.toString() : null);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            System.out.println("[TaskRepo] Due date set for task id=" + taskId + ": " + dueDate);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update due date", e);
        }
    }

    // ── UPDATE PRIORITY ───────────────────────────────────────

    public void updatePriority(int taskId, Task.Priority priority) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PRIORITY)) {
            stmt.setString(1, priority.name());
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            System.out.println("[TaskRepo] Priority set for task id=" + taskId + ": " + priority);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update priority", e);
        }
    }

    // ── UPDATE NOTES ──────────────────────────────────────────

    /**
     * Saves task-specific notes.
     * Called when user finishes editing the task notes TextArea.
     */
    public void updateNotes(int taskId, String notes) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_NOTES)) {
            stmt.setString(1, notes == null ? "" : notes);
            stmt.setInt(2, taskId);
            stmt.executeUpdate();
            System.out.println("[TaskRepo] Notes saved for task id=" + taskId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update task notes", e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public void delete(int taskId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    // ── COUNT TOTAL ───────────────────────────────────────────

    public int countTotal(int projectId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_TOTAL)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TaskRepo] CountTotal failed: " + e.getMessage());
        }
        return 0;
    }

    // ── COUNT COMPLETED ───────────────────────────────────────

    public int countCompleted(int projectId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_COMPLETED)) {
            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[TaskRepo] CountCompleted failed: " + e.getMessage());
        }
        return 0;
    }

    // ── MAP ROW ───────────────────────────────────────────────

    private Task mapRow(ResultSet rs) throws SQLException {
        String dueDateStr = rs.getString("due_date");
        LocalDate dueDate = (dueDateStr != null && !dueDateStr.isEmpty())
            ? LocalDate.parse(dueDateStr) : null;

        Task.Priority priority = Task.Priority.fromString(rs.getString("priority"));

        String notes = rs.getString("notes");

        return new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getInt("completed") == 1,
            LocalDateTime.parse(rs.getString("created_at")),
            rs.getInt("stage_id"),
            rs.getInt("project_id"),
            dueDate,
            priority,
            notes
        );
    }
}

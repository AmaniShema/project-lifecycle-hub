package com.lifecyclehub.repository;

import com.lifecyclehub.database.DatabaseConnection;
import com.lifecyclehub.entity.Project;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ProjectRepository — all SQL for the projects table.
 * v2.1.0 — added updateNameAndDescription()
 */
public class ProjectRepository {

    private static final String INSERT =
        "INSERT INTO projects " +
        "(name, description, notes, created_at, current_stage, archived) " +
        "VALUES (?, ?, ?, ?, ?, 0)";

    private static final String FIND_ALL =
        "SELECT id, name, description, notes, created_at, " +
        "current_stage, archived FROM projects ORDER BY created_at DESC";

    private static final String FIND_ACTIVE =
        "SELECT id, name, description, notes, created_at, " +
        "current_stage, archived FROM projects " +
        "WHERE archived = 0 ORDER BY created_at DESC";

    private static final String FIND_ARCHIVED =
        "SELECT id, name, description, notes, created_at, " +
        "current_stage, archived FROM projects " +
        "WHERE archived = 1 ORDER BY created_at DESC";

    private static final String FIND_BY_ID =
        "SELECT id, name, description, notes, created_at, " +
        "current_stage, archived FROM projects WHERE id = ?";

    private static final String UPDATE =
        "UPDATE projects SET name = ?, description = ?, " +
        "notes = ?, current_stage = ?, archived = ? WHERE id = ?";

    private static final String UPDATE_NOTES =
        "UPDATE projects SET notes = ? WHERE id = ?";

    private static final String UPDATE_NAME_DESC =
        "UPDATE projects SET name = ?, description = ? WHERE id = ?";

    private static final String UPDATE_ARCHIVED =
        "UPDATE projects SET archived = ? WHERE id = ?";

    private static final String DELETE =
        "DELETE FROM projects WHERE id = ?";

    // ── INSERT ────────────────────────────────────────────────

    public int insert(Project project) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getNotes());
            stmt.setString(4, project.getCreatedAt().toString());
            stmt.setString(5, project.getCurrentStage());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    project.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert project", e);
        }
        return -1;
    }

    // ── FIND ALL ──────────────────────────────────────────────

    public List<Project> findAll() {
        return queryList(FIND_ALL, null);
    }

    // ── FIND ACTIVE ───────────────────────────────────────────

    public List<Project> findActive() {
        return queryList(FIND_ACTIVE, null);
    }

    // ── FIND ARCHIVED ─────────────────────────────────────────

    public List<Project> findArchived() {
        return queryList(FIND_ARCHIVED, null);
    }

    // ── FIND BY ID ────────────────────────────────────────────

    public Optional<Project> findById(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find project id=" + id, e);
        }
        return Optional.empty();
    }

    // ── UPDATE FULL ───────────────────────────────────────────

    public void update(Project project) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {
            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getNotes());
            stmt.setString(4, project.getCurrentStage());
            stmt.setInt(5, project.isArchived() ? 1 : 0);
            stmt.setInt(6, project.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update project", e);
        }
    }

    // ── UPDATE NOTES ONLY ─────────────────────────────────────

    public void updateNotes(int projectId, String notes) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_NOTES)) {
            stmt.setString(1, notes);
            stmt.setInt(2, projectId);
            stmt.executeUpdate();
            System.out.println("[ProjectRepo] Notes saved for project id=" + projectId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save notes", e);
        }
    }

    // ── UPDATE NAME AND DESCRIPTION ───────────────────────────

    /**
     * Efficiently updates only the name and description of a project.
     * Called from the Edit Project dialog.
     */
    public void updateNameAndDescription(int projectId, String name, String description) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_NAME_DESC)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, projectId);
            stmt.executeUpdate();
            System.out.println("[ProjectRepo] Updated name/desc for id=" + projectId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update project name/desc", e);
        }
    }

    // ── UPDATE ARCHIVED ───────────────────────────────────────

    public void updateArchived(int projectId, boolean archived) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_ARCHIVED)) {
            stmt.setInt(1, archived ? 1 : 0);
            stmt.setInt(2, projectId);
            stmt.executeUpdate();
            System.out.println("[ProjectRepo] Project id=" + projectId
                + (archived ? " archived." : " unarchived."));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update archived status", e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────

    public void delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete project id=" + id, e);
        }
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────

    private List<Project> queryList(String sql, Object param) {
        List<Project> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (param instanceof Integer)
                stmt.setInt(1, (Integer) param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query projects", e);
        }
        return list;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("notes"),
            LocalDateTime.parse(rs.getString("created_at")),
            rs.getString("current_stage"),
            rs.getInt("archived") == 1
        );
    }
}

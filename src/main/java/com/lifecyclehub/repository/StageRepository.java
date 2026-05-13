package com.lifecyclehub.repository;

import com.lifecyclehub.database.DatabaseConnection;
import com.lifecyclehub.entity.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StageRepository — all SQL for the stages table.
 * v1.1.0 — includes notes column.
 */
public class StageRepository {

    private static final String INSERT =
            "INSERT INTO stages (name, project_id, notes) VALUES (?, ?, '')";

    private static final String FIND_BY_PROJECT =
            "SELECT id, name, project_id, notes FROM stages " +
                    "WHERE project_id = ? ORDER BY id ASC";

    private static final String FIND_BY_NAME_AND_PROJECT =
            "SELECT id, name, project_id, notes FROM stages " +
                    "WHERE name = ? AND project_id = ?";

    private static final String UPDATE_NOTES =
            "UPDATE stages SET notes = ? WHERE id = ?";

    // ── INSERT ──

    public int insert(Stage stage) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, stage.getName());
            stmt.setInt(2, stage.getProjectId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    stage.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert stage", e);
        }
        return -1;
    }

    // ── FIND BY PROJECT ──

    public List<Stage> findByProjectId(int projectId) {
        List<Stage> stages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_PROJECT)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) stages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch stages", e);
        }
        return stages;
    }

    // ── FIND BY NAME AND PROJECT ──

    public Stage findByNameAndProjectId(String name, int projectId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     FIND_BY_NAME_AND_PROJECT)) {

            stmt.setString(1, name);
            stmt.setInt(2, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find stage", e);
        }
        return null;
    }

    // ── UPDATE NOTES ──

    /**
     * Saves notes for a specific stage.
     *
     * @param stageId the stage ID
     * @param notes   the notes content
     */
    public void updateNotes(int stageId, String notes) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_NOTES)) {
            stmt.setString(1, notes);
            stmt.setInt(2, stageId);
            stmt.executeUpdate();
            System.out.println("[StageRepo] Notes saved for stage id=" + stageId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save stage notes", e);
        }
    }

    // ── MAP ROW ──

    private Stage mapRow(ResultSet rs) throws SQLException {
        return new Stage(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("project_id"),
                rs.getString("notes")
        );
    }
}
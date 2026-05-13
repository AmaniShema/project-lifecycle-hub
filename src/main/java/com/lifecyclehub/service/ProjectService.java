package com.lifecyclehub.service;

import com.lifecyclehub.entity.Project;
import com.lifecyclehub.entity.Stage;
import com.lifecyclehub.repository.ProjectRepository;
import com.lifecyclehub.repository.StageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ProjectService — business logic for Project operations.
 * v2.1.0 — added editProject()
 */
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final StageRepository   stageRepository;

    public ProjectService() {
        this.projectRepository = new ProjectRepository();
        this.stageRepository   = new StageRepository();
    }

    // ── CREATE ────────────────────────────────────────────────

    public Project createProject(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty.");
        }
        Project project = new Project(
            name.trim(),
            description == null ? "" : description.trim(),
            LocalDateTime.now(),
            Stage.IDEA
        );
        int projectId = projectRepository.insert(project);
        for (String stageName : Stage.ALL_STAGES) {
            stageRepository.insert(new Stage(stageName, projectId));
        }
        System.out.println("[ProjectService] Created project '" + name
            + "' with 6 stages. id=" + projectId);
        return project;
    }

    // ── GET ALL ───────────────────────────────────────────────

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // ── GET BY ID ─────────────────────────────────────────────

    public Project getProjectById(int id) {
        Optional<Project> result = projectRepository.findById(id);
        return result.orElseThrow(() ->
            new RuntimeException("Project not found with id=" + id));
    }

    // ── UPDATE ────────────────────────────────────────────────

    public void updateProject(Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty.");
        }
        projectRepository.update(project);
        System.out.println("[ProjectService] Updated project id=" + project.getId());
    }

    // ── EDIT NAME AND DESCRIPTION ─────────────────────────────

    /**
     * Updates only the name and description of an existing project.
     * Called from the Edit Project dialog in ProjectDetailController.
     *
     * @param projectId      the project to edit
     * @param newName        new name (must not be blank)
     * @param newDescription new description (can be empty)
     */
    public void editProject(int projectId, String newName, String newDescription) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty.");
        }
        projectRepository.updateNameAndDescription(
            projectId,
            newName.trim(),
            newDescription == null ? "" : newDescription.trim()
        );
        System.out.println("[ProjectService] Edited project id=" + projectId
            + " new name='" + newName + "'");
    }

    // ── DELETE ────────────────────────────────────────────────

    public void deleteProject(int projectId) {
        projectRepository.delete(projectId);
        System.out.println("[ProjectService] Deleted project id=" + projectId);
    }

    // ── SAVE NOTES ────────────────────────────────────────────

    public void saveNotes(int projectId, String notes) {
        projectRepository.updateNotes(projectId, notes);
        System.out.println("[ProjectService] Notes saved for project id=" + projectId);
    }

    // ── ARCHIVE / UNARCHIVE ───────────────────────────────────

    public void archiveProject(int projectId) {
        projectRepository.updateArchived(projectId, true);
        System.out.println("[ProjectService] Archived project id=" + projectId);
    }

    public void unarchiveProject(int projectId) {
        projectRepository.updateArchived(projectId, false);
        System.out.println("[ProjectService] Unarchived project id=" + projectId);
    }

    public List<Project> getActiveProjects() {
        return projectRepository.findActive();
    }

    public List<Project> getArchivedProjects() {
        return projectRepository.findArchived();
    }

    // ── ADVANCE STAGE ─────────────────────────────────────────

    public void advanceStage(Project project) {
        String[] stages = Stage.ALL_STAGES;
        String current  = project.getCurrentStage();
        for (int i = 0; i < stages.length - 1; i++) {
            if (stages[i].equals(current)) {
                project.setCurrentStage(stages[i + 1]);
                projectRepository.update(project);
                System.out.println("[ProjectService] Advanced project id="
                    + project.getId() + " to: " + stages[i + 1]);
                return;
            }
        }
        System.out.println("[ProjectService] Already at final stage.");
    }
}

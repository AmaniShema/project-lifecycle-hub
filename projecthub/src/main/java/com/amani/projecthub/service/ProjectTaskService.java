package com.amani.projecthub.service;

import com.amani.projecthub.dto.CreateTaskRequest;
import com.amani.projecthub.dto.ProjectTaskDTO;
import com.amani.projecthub.entity.Project;
import com.amani.projecthub.entity.ProjectTask;
import com.amani.projecthub.entity.Stage;
import com.amani.projecthub.repository.ProjectRepository;
import com.amani.projecthub.repository.ProjectTaskRepository;
import com.amani.projecthub.repository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectTaskService {

    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;

    public ProjectTaskService(ProjectTaskRepository projectTaskRepository,
                              ProjectRepository projectRepository,
                              StageRepository stageRepository) {
        this.projectTaskRepository = projectTaskRepository;
        this.projectRepository = projectRepository;
        this.stageRepository = stageRepository;
    }

    // ── Create Task ───────────────────────────────────────────────────────────
    // A task must belong to both a Project and a Stage.
    // We validate both exist before creating the task.

    public ProjectTaskDTO createTask(Long projectId, CreateTaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Stage stage = stageRepository.findById(request.getStageId())
                .orElseThrow(() -> new RuntimeException("Stage not found with id: " + request.getStageId()));

        // Ensure the stage actually belongs to this project
        if (!stage.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "Stage " + request.getStageId() + " does not belong to project " + projectId
            );
        }

        ProjectTask task = new ProjectTask(request.getTitle(), stage, project);
        ProjectTask saved = projectTaskRepository.save(task);
        return mapToDTO(saved);
    }

    // ── Get Tasks by Project ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectTaskDTO> getTasksByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        return projectTaskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Get Tasks by Stage ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectTaskDTO> getTasksByStage(Long stageId) {
        if (!stageRepository.existsById(stageId)) {
            throw new RuntimeException("Stage not found with id: " + stageId);
        }
        return projectTaskRepository.findByStageId(stageId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Mark Task Complete ────────────────────────────────────────────────────

    public ProjectTaskDTO markTaskCompleted(Long taskId) {
        ProjectTask task = findTaskOrThrow(taskId);
        task.setCompleted(true);
        return mapToDTO(projectTaskRepository.save(task));
    }

    // ── Mark Task Incomplete ──────────────────────────────────────────────────

    public ProjectTaskDTO markTaskIncomplete(Long taskId) {
        ProjectTask task = findTaskOrThrow(taskId);
        task.setCompleted(false);
        return mapToDTO(projectTaskRepository.save(task));
    }

    // ── Delete Task ───────────────────────────────────────────────────────────

    public void deleteTask(Long taskId) {
        if (!projectTaskRepository.existsById(taskId)) {
            throw new RuntimeException("Task not found with id: " + taskId);
        }
        projectTaskRepository.deleteById(taskId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private ProjectTask findTaskOrThrow(Long taskId) {
        return projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
    }

    // ── Entity → DTO Mapping ──────────────────────────────────────────────────

    private ProjectTaskDTO mapToDTO(ProjectTask task) {
        ProjectTaskDTO dto = new ProjectTaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setCompleted(task.isCompleted());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setStageId(task.getStage().getId());
        dto.setStageName(task.getStage().getName());
        dto.setProjectId(task.getProject().getId());
        return dto;
    }
}
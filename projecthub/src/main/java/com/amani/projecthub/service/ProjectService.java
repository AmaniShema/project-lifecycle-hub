package com.amani.projecthub.service;

import com.amani.projecthub.dto.CreateProjectRequest;
import com.amani.projecthub.dto.ProjectDTO;
import com.amani.projecthub.dto.ProjectTaskDTO;
import com.amani.projecthub.dto.StageDTO;
import com.amani.projecthub.entity.Milestone;
import com.amani.projecthub.entity.Project;
import com.amani.projecthub.entity.ProjectTask;
import com.amani.projecthub.entity.Stage;
import com.amani.projecthub.entity.User;
import com.amani.projecthub.repository.MilestoneRepository;
import com.amani.projecthub.repository.ProjectRepository;
import com.amani.projecthub.repository.ProjectTaskRepository;
import com.amani.projecthub.repository.StageRepository;
import com.amani.projecthub.security.AuthUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {

    private static final List<String> DEFAULT_STAGES = Arrays.asList(
            "IDEA", "PLANNING", "BUILDING", "TESTING", "LAUNCH", "MAINTENANCE"
    );

    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final MilestoneRepository milestoneRepository;
    private final AuthUtils authUtils;

    public ProjectService(ProjectRepository projectRepository,
                          StageRepository stageRepository,
                          ProjectTaskRepository projectTaskRepository,
                          MilestoneRepository milestoneRepository,
                          AuthUtils authUtils) {
        this.projectRepository = projectRepository;
        this.stageRepository = stageRepository;
        this.projectTaskRepository = projectTaskRepository;
        this.milestoneRepository = milestoneRepository;
        this.authUtils = authUtils;
    }

    // ── Create Project ────────────────────────────────────────────────────────

    public ProjectDTO createProject(CreateProjectRequest request) {
        User owner = authUtils.getCurrentUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        Project savedProject = projectRepository.save(project);

        DEFAULT_STAGES.forEach(stageName -> {
            Stage stage = new Stage(stageName, savedProject);
            stageRepository.save(stage);
        });

        return mapToDTO(savedProject);
    }

    // ── Get All Projects for Current User ─────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        User owner = authUtils.getCurrentUser();
        return projectRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Get Single Project — must belong to current user ──────────────────────

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = findProjectForCurrentUser(id);
        return mapToDTOWithStages(project);
    }

    // ── Update Current Stage ──────────────────────────────────────────────────

    public ProjectDTO updateCurrentStage(Long id, String newStage) {
        validateStage(newStage);
        Project project = findProjectForCurrentUser(id);
        project.setCurrentStage(newStage);
        return mapToDTO(projectRepository.save(project));
    }

    // ── Delete Project ────────────────────────────────────────────────────────

    public void deleteProject(Long id) {
        Project project = findProjectForCurrentUser(id);
        projectRepository.delete(project);
    }

    // ── Progress Calculation ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public double calculateProgress(Long projectId) {
        long total = projectTaskRepository.countByProjectId(projectId);
        if (total == 0) return 0.0;
        long completed = projectTaskRepository
                .countByProjectIdAndCompleted(projectId, true);
        return Math.round(((double) completed / total) * 100.0 * 10.0) / 10.0;
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Project findProjectForCurrentUser(Long id) {
        User owner = authUtils.getCurrentUser();
        return projectRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id));
    }

    private void validateStage(String stage) {
        if (!DEFAULT_STAGES.contains(stage)) {
            throw new IllegalArgumentException(
                    "Invalid stage: " + stage +
                            ". Must be one of: " + DEFAULT_STAGES);
        }
    }

    // ── Entity → DTO Mapping ──────────────────────────────────────────────────

    private ProjectDTO mapToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCurrentStage(project.getCurrentStage());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setProgress(calculateProgress(project.getId()));

        long total = projectTaskRepository.countByProjectId(project.getId());
        long completed = projectTaskRepository
                .countByProjectIdAndCompleted(project.getId(), true);
        dto.setTotalTasks(total);
        dto.setCompletedTasks(completed);

        populateMilestoneSummary(dto, project.getId());
        return dto;
    }

    private ProjectDTO mapToDTOWithStages(Project project) {
        ProjectDTO dto = mapToDTO(project);
        List<StageDTO> stageDTOs = stageRepository
                .findByProjectId(project.getId())
                .stream()
                .map(this::mapStageToDTO)
                .collect(Collectors.toList());
        dto.setStages(stageDTOs);
        return dto;
    }

    private void populateMilestoneSummary(ProjectDTO dto, Long projectId) {
        List<Milestone> milestones = milestoneRepository
                .findByProjectIdOrderByDueDateAsc(projectId);

        dto.setTotalMilestones(milestones.size());

        LocalDate today = LocalDate.now();

        long overdueCount = milestones.stream()
                .filter(m -> !m.isCompleted() && m.getDueDate().isBefore(today))
                .count();
        dto.setOverdueMilestones((int) overdueCount);

        milestones.stream()
                .filter(m -> !m.isCompleted() && !m.getDueDate().isBefore(today))
                .findFirst()
                .ifPresent(m -> {
                    dto.setUpcomingMilestoneTitle(m.getTitle());
                    dto.setUpcomingMilestoneDueDate(m.getDueDate());
                    dto.setUpcomingMilestoneDaysLeft(
                            ChronoUnit.DAYS.between(today, m.getDueDate()));
                });
    }

    private StageDTO mapStageToDTO(Stage stage) {
        StageDTO dto = new StageDTO();
        dto.setId(stage.getId());
        dto.setName(stage.getName());
        dto.setProjectId(stage.getProject().getId());

        List<ProjectTaskDTO> taskDTOs = projectTaskRepository
                .findByStageId(stage.getId())
                .stream()
                .map(this::mapTaskToDTO)
                .collect(Collectors.toList());

        dto.setTasks(taskDTOs);
        return dto;
    }

    private ProjectTaskDTO mapTaskToDTO(ProjectTask task) {
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
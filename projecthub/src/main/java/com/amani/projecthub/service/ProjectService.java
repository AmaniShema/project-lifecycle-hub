package com.amani.projecthub.service;

import com.amani.projecthub.model.Project;
import com.amani.projecthub.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.amani.projecthub.repository.StageRepository;
import com.amani.projecthub.model.Stage;
import java.util.Arrays;


@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;

    public ProjectService(ProjectRepository projectRepository, StageRepository stageRepository) {
        this.projectRepository = projectRepository;
        this.stageRepository = stageRepository;
    }

    // Create a new project
    public Project createProject(String name, String description) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCurrentStage("IDEA");

        Project savedProject = projectRepository.save(project);

        // Default life-cycle stages
        String[] defaultStages = {"IDEA", "PLANNING", "BUILDING", "TESTING", "LAUNCH", "MAINTENANCE"};

        Arrays.stream(defaultStages).forEach(stageName -> {
            Stage stage = new Stage(stageName, savedProject);
            stageRepository.save(stage);
        });

        return savedProject;
    }

    // Get all projects
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Get project by id
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    // Update project stage
    public Project updateStage(Long projectId, String newStage) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setCurrentStage(newStage);
        return projectRepository.save(project);
    }

    // Delete project (optional for MVP)
    public void deleteProject(Long projectId) {
        projectRepository.deleteById(projectId);
    }
}

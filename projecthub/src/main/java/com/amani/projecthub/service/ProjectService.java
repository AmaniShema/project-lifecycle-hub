package com.amani.projecthub.service;

import com.amani.projecthub.model.Project;
import com.amani.projecthub.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Create a new project
    public Project createProject(String name, String description) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCurrentStage("IDEA"); // default starting stage
        return projectRepository.save(project);
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

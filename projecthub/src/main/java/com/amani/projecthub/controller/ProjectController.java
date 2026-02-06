package com.amani.projecthub.controller;

import com.amani.projecthub.model.Project;
import com.amani.projecthub.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*") // allow frontend calls later
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create a new project
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest request) {
        Project created = projectService.createProject(request.getName(), request.getDescription());
        return ResponseEntity.ok(created);
    }

    // Get all projects
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // Get project by id
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update project stage
    @PatchMapping("/{id}/stage")
    public ResponseEntity<Project> updateStage(
            @PathVariable Long id,
            @RequestParam String stage
    ) {
        Project updated = projectService.updateStage(id, stage);
        return ResponseEntity.ok(updated);
    }

    // --- Simple DTO for create request ---
    public static class CreateProjectRequest {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}

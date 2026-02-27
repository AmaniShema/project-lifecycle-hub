package com.amani.projecthub.controller;

import com.amani.projecthub.dto.CreateProjectRequest;
import com.amani.projecthub.dto.ProjectDTO;
import com.amani.projecthub.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // POST /api/projects
    // Create a new project — stages are auto-created by the service
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDTO created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/projects
    // Returns all projects with dynamically calculated progress
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    // GET /api/projects/{id}
    // Returns one project including all its stages and tasks
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    // PATCH /api/projects/{id}/stage
    // Move the project to a different life-cycle stage
    @PatchMapping("/{id}/stage")
    public ResponseEntity<ProjectDTO> updateStage(
            @PathVariable Long id,
            @RequestParam String stage) {
        return ResponseEntity.ok(projectService.updateCurrentStage(id, stage));
    }

    // DELETE /api/projects/{id}
    // Delete a project and all its associated stages and tasks
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
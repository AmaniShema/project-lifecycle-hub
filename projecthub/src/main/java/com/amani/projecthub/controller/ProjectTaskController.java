package com.amani.projecthub.controller;

import com.amani.projecthub.dto.CreateTaskRequest;
import com.amani.projecthub.dto.ProjectTaskDTO;
import com.amani.projecthub.service.ProjectTaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    // POST /api/projects/{projectId}/tasks
    // Create a new task inside a specific stage of a project
    @PostMapping("/{projectId}/tasks")
    public ResponseEntity<ProjectTaskDTO> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        ProjectTaskDTO created = projectTaskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/projects/{projectId}/tasks
    // Get all tasks for a project across all stages
    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<ProjectTaskDTO>> getTasksByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectTaskService.getTasksByProject(projectId));
    }

    // GET /api/projects/{projectId}/tasks/stage/{stageId}
    // Get tasks filtered by a specific stage
    @GetMapping("/{projectId}/tasks/stage/{stageId}")
    public ResponseEntity<List<ProjectTaskDTO>> getTasksByStage(
            @PathVariable Long projectId,
            @PathVariable Long stageId) {
        return ResponseEntity.ok(projectTaskService.getTasksByStage(stageId));
    }

    // PATCH /api/projects/{projectId}/tasks/{taskId}/complete
    // Mark a task as completed — this affects progress calculation
    @PatchMapping("/{projectId}/tasks/{taskId}/complete")
    public ResponseEntity<ProjectTaskDTO> completeTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(projectTaskService.markTaskCompleted(taskId));
    }

    // PATCH /api/projects/{projectId}/tasks/{taskId}/incomplete
    // Revert a task back to incomplete
    @PatchMapping("/{projectId}/tasks/{taskId}/incomplete")
    public ResponseEntity<ProjectTaskDTO> uncompleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(projectTaskService.markTaskIncomplete(taskId));
    }

    // DELETE /api/projects/{projectId}/tasks/{taskId}
    // Delete a task
    @DeleteMapping("/{projectId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        projectTaskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
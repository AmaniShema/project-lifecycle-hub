package com.amani.projecthub.controller;

import com.amani.projecthub.model.Task;
import com.amani.projecthub.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1️⃣ Create a task
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(
            @RequestParam Long projectId,
            @RequestParam Long stageId,
            @RequestParam String title
    ) {
        return taskService.createTask(projectId, stageId, title);
    }

    // 2️⃣ Get all tasks for a project
    @GetMapping("/project/{projectId}")
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    // 3️⃣ Get tasks by stage
    @GetMapping("/stage/{stageId}")
    public List<Task> getTasksByStage(@PathVariable Long stageId) {
        return taskService.getTasksByStage(stageId);
    }

    // 4️⃣ Mark task as completed
    @PatchMapping("/{taskId}/complete")
    public Task completeTask(@PathVariable Long taskId) {
        return taskService.markTaskCompleted(taskId);
    }

    // 5️⃣ Delete task (optional but useful)
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }
}

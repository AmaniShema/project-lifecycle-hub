package com.amani.projecthub.service;

import com.amani.projecthub.model.Project;
import com.amani.projecthub.model.Stage;
import com.amani.projecthub.model.Task;
import com.amani.projecthub.repository.ProjectRepository;
import com.amani.projecthub.repository.StageRepository;
import com.amani.projecthub.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       StageRepository stageRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.stageRepository = stageRepository;
    }

    // Create a new task inside a specific stage
    public Task createTask(Long projectId, Long stageId, String title) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage not found"));

        Task task = new Task(title, stage, project);

        return taskRepository.save(task);
    }

    // Get all tasks for a project
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // Get tasks by stage
    public List<Task> getTasksByStage(Long stageId) {
        return taskRepository.findByStageId(stageId);
    }

    // Mark task as completed
    public Task markTaskCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setCompleted(true);

        return taskRepository.save(task);
    }

    // Delete task (optional but useful)
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}

package com.amani.projecthub.repository;

import com.amani.projecthub.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    // All tasks belonging to a project
    // SELECT * FROM tasks WHERE project_id = ?
    List<ProjectTask> findByProjectId(Long projectId);

    // All tasks belonging to a specific stage
    // SELECT * FROM tasks WHERE stage_id = ?
    List<ProjectTask> findByStageId(Long stageId);

    // Count total tasks for a project — used for progress calculation
    // SELECT COUNT(*) FROM tasks WHERE project_id = ?
    long countByProjectId(Long projectId);

    // Count only completed tasks for a project — used for progress calculation
    // SELECT COUNT(*) FROM tasks WHERE project_id = ? AND completed = true
    long countByProjectIdAndCompleted(Long projectId, boolean completed);
}
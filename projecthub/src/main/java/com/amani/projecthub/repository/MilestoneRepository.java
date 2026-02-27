package com.amani.projecthub.repository;

import com.amani.projecthub.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    // All milestones for a project ordered by due date ascending
    // So the nearest deadline always appears first
    List<Milestone> findByProjectIdOrderByDueDateAsc(Long projectId);
}
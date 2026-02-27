package com.amani.projecthub.repository;

import com.amani.projecthub.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    // Spring Data JPA generates the SQL for this automatically
    // based on the method name: SELECT * FROM stages WHERE project_id = ?
    List<Stage> findByProjectId(Long projectId);

    // Used when deleting a project's stages
    void deleteByProjectId(Long projectId);
}
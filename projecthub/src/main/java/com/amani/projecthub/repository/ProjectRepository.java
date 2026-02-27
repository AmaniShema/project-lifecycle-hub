package com.amani.projecthub.repository;

import com.amani.projecthub.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Only return projects belonging to a specific owner
    List<Project> findByOwnerId(Long ownerId);

    // Find project by id only if it belongs to the owner
    // Prevents users from accessing other users' projects
    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
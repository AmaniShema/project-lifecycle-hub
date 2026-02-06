package com.amani.projecthub.repository;

import com.amani.projecthub.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // JpaRepository already gives you:
    // save(), findAll(), findById(), deleteById(), etc.
}

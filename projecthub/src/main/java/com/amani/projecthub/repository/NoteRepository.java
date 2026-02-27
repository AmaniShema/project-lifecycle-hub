package com.amani.projecthub.repository;

import com.amani.projecthub.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Notes for a specific stage, newest first
    List<Note> findByStageIdOrderByCreatedAtDesc(Long stageId);

    // All notes across all stages for a project, newest first
    List<Note> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
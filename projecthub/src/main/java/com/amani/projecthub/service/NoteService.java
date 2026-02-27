package com.amani.projecthub.service;

import com.amani.projecthub.dto.CreateNoteRequest;
import com.amani.projecthub.dto.NoteDTO;
import com.amani.projecthub.entity.Note;
import com.amani.projecthub.entity.Project;
import com.amani.projecthub.entity.Stage;
import com.amani.projecthub.repository.NoteRepository;
import com.amani.projecthub.repository.ProjectRepository;
import com.amani.projecthub.repository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final ProjectRepository projectRepository;
    private final StageRepository stageRepository;

    public NoteService(NoteRepository noteRepository,
                       ProjectRepository projectRepository,
                       StageRepository stageRepository) {
        this.noteRepository = noteRepository;
        this.projectRepository = projectRepository;
        this.stageRepository = stageRepository;
    }

    // ── Create Note ───────────────────────────────────────────────────────────
    // A note must belong to both a project and a specific stage.
    // We validate the stage belongs to the project before saving.

    public NoteDTO createNote(Long projectId, Long stageId,
                              CreateNoteRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + projectId));

        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() ->
                        new RuntimeException("Stage not found with id: " + stageId));

        if (!stage.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException(
                    "Stage " + stageId + " does not belong to project " + projectId);
        }

        Note note = new Note(request.getContent(), stage, project);
        return mapToDTO(noteRepository.save(note));
    }

    // ── Get Notes by Stage ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NoteDTO> getNotesByStage(Long projectId, Long stageId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        if (!stageRepository.existsById(stageId)) {
            throw new RuntimeException("Stage not found with id: " + stageId);
        }
        return noteRepository.findByStageIdOrderByCreatedAtDesc(stageId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Get All Notes for a Project ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NoteDTO> getNotesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        return noteRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Delete Note ───────────────────────────────────────────────────────────

    public void deleteNote(Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            throw new RuntimeException("Note not found with id: " + noteId);
        }
        noteRepository.deleteById(noteId);
    }

    // ── Entity → DTO Mapping ──────────────────────────────────────────────────

    private NoteDTO mapToDTO(Note note) {
        NoteDTO dto = new NoteDTO();
        dto.setId(note.getId());
        dto.setContent(note.getContent());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setStageId(note.getStage().getId());
        dto.setStageName(note.getStage().getName());
        dto.setProjectId(note.getProject().getId());
        return dto;
    }
}
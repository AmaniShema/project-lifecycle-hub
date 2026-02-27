package com.amani.projecthub.controller;

import com.amani.projecthub.dto.CreateNoteRequest;
import com.amani.projecthub.dto.NoteDTO;
import com.amani.projecthub.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // POST /api/projects/{projectId}/stages/{stageId}/notes
    @PostMapping("/{projectId}/stages/{stageId}/notes")
    public ResponseEntity<NoteDTO> createNote(
            @PathVariable Long projectId,
            @PathVariable Long stageId,
            @Valid @RequestBody CreateNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.createNote(projectId, stageId, request));
    }

    // GET /api/projects/{projectId}/stages/{stageId}/notes
    @GetMapping("/{projectId}/stages/{stageId}/notes")
    public ResponseEntity<List<NoteDTO>> getNotesByStage(
            @PathVariable Long projectId,
            @PathVariable Long stageId) {
        return ResponseEntity.ok(noteService.getNotesByStage(projectId, stageId));
    }

    // GET /api/projects/{projectId}/notes
    @GetMapping("/{projectId}/notes")
    public ResponseEntity<List<NoteDTO>> getNotesByProject(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(noteService.getNotesByProject(projectId));
    }

    // DELETE /api/projects/{projectId}/notes/{noteId}
    @DeleteMapping("/{projectId}/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long projectId,
            @PathVariable Long noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }
}
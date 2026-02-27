package com.amani.projecthub.controller;

import com.amani.projecthub.dto.CreateMilestoneRequest;
import com.amani.projecthub.dto.MilestoneDTO;
import com.amani.projecthub.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    // POST /api/projects/{projectId}/milestones
    @PostMapping("/{projectId}/milestones")
    public ResponseEntity<MilestoneDTO> createMilestone(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(milestoneService.createMilestone(projectId, request));
    }

    // GET /api/projects/{projectId}/milestones
    @GetMapping("/{projectId}/milestones")
    public ResponseEntity<List<MilestoneDTO>> getMilestones(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(milestoneService.getMilestonesByProject(projectId));
    }

    // PATCH /api/projects/{projectId}/milestones/{milestoneId}/complete
    @PatchMapping("/{projectId}/milestones/{milestoneId}/complete")
    public ResponseEntity<MilestoneDTO> completeMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId) {
        return ResponseEntity.ok(milestoneService.markCompleted(milestoneId));
    }

    // PATCH /api/projects/{projectId}/milestones/{milestoneId}/incomplete
    @PatchMapping("/{projectId}/milestones/{milestoneId}/incomplete")
    public ResponseEntity<MilestoneDTO> incompleteMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId) {
        return ResponseEntity.ok(milestoneService.markIncomplete(milestoneId));
    }

    // DELETE /api/projects/{projectId}/milestones/{milestoneId}
    @DeleteMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<Void> deleteMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId) {
        milestoneService.deleteMilestone(milestoneId);
        return ResponseEntity.noContent().build();
    }
}
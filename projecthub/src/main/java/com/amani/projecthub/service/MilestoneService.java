package com.amani.projecthub.service;

import com.amani.projecthub.dto.CreateMilestoneRequest;
import com.amani.projecthub.dto.MilestoneDTO;
import com.amani.projecthub.entity.Milestone;
import com.amani.projecthub.entity.Project;
import com.amani.projecthub.repository.MilestoneRepository;
import com.amani.projecthub.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    public MilestoneService(MilestoneRepository milestoneRepository,
                            ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    // ── Create Milestone ──────────────────────────────────────────────────────

    public MilestoneDTO createMilestone(Long projectId, CreateMilestoneRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        Milestone milestone = new Milestone(
                request.getTitle(),
                request.getDueDate(),
                project
        );

        return mapToDTO(milestoneRepository.save(milestone));
    }

    // ── Get All Milestones for a Project ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MilestoneDTO> getMilestonesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ── Mark Milestone Complete ───────────────────────────────────────────────

    public MilestoneDTO markCompleted(Long milestoneId) {
        Milestone milestone = findOrThrow(milestoneId);
        milestone.setCompleted(true);
        return mapToDTO(milestoneRepository.save(milestone));
    }

    // ── Mark Milestone Incomplete ─────────────────────────────────────────────

    public MilestoneDTO markIncomplete(Long milestoneId) {
        Milestone milestone = findOrThrow(milestoneId);
        milestone.setCompleted(false);
        return mapToDTO(milestoneRepository.save(milestone));
    }

    // ── Delete Milestone ──────────────────────────────────────────────────────

    public void deleteMilestone(Long milestoneId) {
        if (!milestoneRepository.existsById(milestoneId)) {
            throw new RuntimeException("Milestone not found with id: " + milestoneId);
        }
        milestoneRepository.deleteById(milestoneId);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Milestone findOrThrow(Long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found with id: " + id));
    }

    // ── Entity → DTO Mapping ──────────────────────────────────────────────────
    // We compute overdue and daysUntilDue here dynamically
    // These values are never stored in the database

    private MilestoneDTO mapToDTO(Milestone milestone) {
        MilestoneDTO dto = new MilestoneDTO();
        dto.setId(milestone.getId());
        dto.setTitle(milestone.getTitle());
        dto.setDueDate(milestone.getDueDate());
        dto.setCompleted(milestone.isCompleted());
        dto.setCreatedAt(milestone.getCreatedAt());
        dto.setProjectId(milestone.getProject().getId());

        LocalDate today = LocalDate.now();
        long daysUntil = ChronoUnit.DAYS.between(today, milestone.getDueDate());
        dto.setDaysUntilDue(daysUntil);
        dto.setOverdue(!milestone.isCompleted() && milestone.getDueDate().isBefore(today));

        return dto;
    }
}
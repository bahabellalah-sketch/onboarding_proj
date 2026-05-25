package com.onboarding.service;

import com.onboarding.dto.AssignmentEvaluationSummaryDTO;
import com.onboarding.dto.CreateAssignmentEvaluationDTO;
import com.onboarding.dto.EvaluationDTO;
import com.onboarding.dto.PendingManagerEvaluationDTO;
import com.onboarding.entity.*;
import com.onboarding.repository.AssignmentRepository;
import com.onboarding.repository.ChecklistRepository;
import com.onboarding.repository.EvaluationRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public Evaluation createStepEvaluation(Long checklistId, Long userId, Integer rating, String comment) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));

        if (checklist.getStatut() != StatutChecklist.TERMINE) {
            throw new RuntimeException("L'évaluation n'est disponible qu'après la fin de l'étape");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!checklist.getAssignment().getUser().getId().equals(userId)) {
            throw new RuntimeException("Vous ne pouvez évaluer que vos propres étapes");
        }

        if (evaluationRepository.existsByUserAndChecklist(user, checklist)) {
            throw new RuntimeException("Vous avez déjà évalué cette étape");
        }

        Evaluation evaluation = new Evaluation(checklist, user, rating, comment);
        Evaluation saved = evaluationRepository.save(evaluation);
        notificationService.sendEvaluationReceivedNotification(saved);
        return saved;
    }

    public Evaluation createAssignmentEvaluation(@NonNull User evaluator, @NonNull CreateAssignmentEvaluationDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignation introuvable"));

        EvaluationType type = dto.getEvaluationType();
        if (type != EvaluationType.PARCOURS_COLLAB && type != EvaluationType.PARCOURS_MANAGER) {
            throw new RuntimeException("Type d'évaluation invalide pour une assignation");
        }

        if (!isAssignmentEligibleForEvaluation(assignment)) {
            throw new RuntimeException("L'assignation doit être terminée (100% ou statut Terminé) pour être évaluée");
        }

        if (type == EvaluationType.PARCOURS_COLLAB) {
            if (evaluator.getRole() != Role.COLLABORATEUR) {
                throw new RuntimeException("Seul le collaborateur peut soumettre un bilan de parcours");
            }
            if (!assignment.getUser().getId().equals(evaluator.getId())) {
                throw new RuntimeException("Vous ne pouvez évaluer que vos propres parcours");
            }
        } else {
            if (evaluator.getRole() != Role.MANAGER && evaluator.getRole() != Role.ADMINISTRATEUR) {
                throw new RuntimeException("Seuls les managers peuvent évaluer un collaborateur");
            }
            if (evaluator.getRole() == Role.MANAGER && !canManagerManageAssignment(evaluator, assignment)) {
                throw new RuntimeException("Vous ne gérez pas ce collaborateur");
            }
            if (dto.getRecommendation() == null || dto.getRecommendation().isBlank()) {
                throw new RuntimeException("La recommandation est obligatoire pour l'évaluation manager");
            }
        }

        if (evaluationRepository.existsByUserAndAssignmentAndEvaluationType(evaluator, assignment, type)) {
            throw new RuntimeException("Une évaluation de ce type existe déjà pour cette assignation");
        }

        Evaluation evaluation = new Evaluation(
                assignment,
                evaluator,
                type,
                dto.getRating(),
                dto.getComment(),
                dto.getRecommendation()
        );
        Evaluation saved = evaluationRepository.save(evaluation);
        notificationService.sendAssignmentEvaluationNotification(saved);
        return saved;
    }

    public boolean isAssignmentEligibleForEvaluation(Assignment assignment) {
        if (assignment.getPourcentageAvancement() != null && assignment.getPourcentageAvancement() >= 100) {
            return true;
        }
        return assignment.getStatut() == StatutAssignment.TERMINE;
    }

    public boolean canManagerManageAssignment(User manager, Assignment assignment) {
        User collaborateur = userRepository.findByIdWithManager(assignment.getUser().getId())
                .orElse(assignment.getUser());
        if (collaborateur.getManager() != null && manager.getId().equals(collaborateur.getManager().getId())) {
            return true;
        }
        return userRepository.findByManagerId(manager.getId()).stream()
                .anyMatch(u -> u.getId().equals(collaborateur.getId()));
    }

    public Evaluation getEvaluationByUserAndChecklist(Long userId, Long checklistId) {
        User user = userRepository.findById(userId).orElse(null);
        Checklist checklist = checklistRepository.findById(checklistId).orElse(null);
        if (user == null || checklist == null) {
            return null;
        }
        return evaluationRepository.findByUserAndChecklist(user, checklist).orElse(null);
    }

    public boolean canUserEvaluateStep(Long userId, Long checklistId) {
        try {
            Checklist checklist = checklistRepository.findById(checklistId)
                    .orElseThrow(() -> new RuntimeException("Checklist not found"));
            if (checklist.getStatut() != StatutChecklist.TERMINE) {
                return false;
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!checklist.getAssignment().getUser().getId().equals(userId)) {
                return false;
            }
            return !evaluationRepository.existsByUserAndChecklist(user, checklist);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public AssignmentEvaluationSummaryDTO getAssignmentSummary(@NonNull User viewer, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignation introuvable"));

        AssignmentEvaluationSummaryDTO summary = new AssignmentEvaluationSummaryDTO();
        summary.setAssignmentId(assignmentId);

        boolean eligible = isAssignmentEligibleForEvaluation(assignment);
        Double avg = evaluationRepository.calculateAverageStepRatingByAssignment(assignment);
        summary.setAverageStepRating(avg != null ? avg : 0.0);
        summary.setStepEvaluationCount(evaluationRepository.countStepEvaluationsByAssignment(assignment));

        evaluationRepository.findByAssignmentAndEvaluationType(assignment, EvaluationType.PARCOURS_COLLAB)
                .stream().findFirst()
                .ifPresent(e -> summary.setCollabEvaluation(toDto(e)));

        evaluationRepository.findByAssignmentAndEvaluationType(assignment, EvaluationType.PARCOURS_MANAGER)
                .stream().findFirst()
                .ifPresent(e -> summary.setManagerEvaluation(toDto(e)));

        if (eligible && viewer.getRole() == Role.COLLABORATEUR
                && assignment.getUser().getId().equals(viewer.getId())) {
            summary.setCanEvaluateCollab(!evaluationRepository.existsByUserAndAssignmentAndEvaluationType(
                    viewer, assignment, EvaluationType.PARCOURS_COLLAB));
        }

        if (eligible && (viewer.getRole() == Role.MANAGER || viewer.getRole() == Role.ADMINISTRATEUR)) {
            if (viewer.getRole() == Role.ADMINISTRATEUR || canManagerManageAssignment(viewer, assignment)) {
                summary.setCanEvaluateManager(!evaluationRepository.existsByUserAndAssignmentAndEvaluationType(
                        viewer, assignment, EvaluationType.PARCOURS_MANAGER));
            }
        }

        return summary;
    }

    @Transactional(readOnly = true)
    public List<PendingManagerEvaluationDTO> getPendingForManager(@NonNull User manager) {
        List<User> team = userRepository.findByManagerId(manager.getId());
        if (team.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = team.stream().map(User::getId).collect(Collectors.toList());
        List<Assignment> assignments = assignmentRepository.findByUserIdIn(userIds);

        List<PendingManagerEvaluationDTO> pending = new ArrayList<>();
        for (Assignment a : assignments) {
            if (!isAssignmentEligibleForEvaluation(a)) {
                continue;
            }
            if (evaluationRepository.existsByUserAndAssignmentAndEvaluationType(
                    manager, a, EvaluationType.PARCOURS_MANAGER)) {
                continue;
            }
            PendingManagerEvaluationDTO dto = new PendingManagerEvaluationDTO();
            dto.setAssignmentId(a.getId());
            User collab = a.getUser();
            dto.setCollaborateurId(collab.getId());
            dto.setCollaborateurNom(collab.getPrenom() + " " + collab.getNom());
            dto.setCollaborateurEmail(collab.getEmail());
            dto.setParcoursNom(a.getParcours().getNom());
            dto.setPourcentageAvancement(a.getPourcentageAvancement());
            dto.setStatut(a.getStatut().name());
            dto.setDateFinReelle(a.getDateFinReelle());
            pending.add(dto);
        }
        return pending;
    }

    public List<EvaluationDTO> getDashboardEvaluations(@NonNull User viewer, String typeFilter) {
        if (viewer.getRole() != Role.ADMINISTRATEUR) {
            throw new SecurityException("Accès réservé aux administrateurs");
        }
        List<Evaluation> all = evaluationRepository.findAllOrderByDateDesc();
        return all.stream()
                .filter(e -> typeFilter == null || typeFilter.isBlank()
                        || e.getEvaluationType().name().equalsIgnoreCase(typeFilter))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Evaluation> getEvaluationsByChecklist(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        return evaluationRepository.findByChecklist(checklist);
    }

    public double calculateAverageRating(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        Double average = evaluationRepository.calculateAverageRatingByChecklist(checklist);
        return average != null ? average : 0.0;
    }

    public EvaluationDTO toDto(Evaluation e) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(e.getId());
        dto.setEvaluationType(e.getEvaluationType());
        dto.setRating(e.getRating());
        dto.setComment(e.getComment());
        dto.setRecommendation(e.getRecommendation());
        dto.setDateEvaluation(e.getDateEvaluation());

        User evaluator = e.getUser();
        if (evaluator != null) {
            dto.setEvaluatorId(evaluator.getId());
            dto.setEvaluatorNom(evaluator.getPrenom() + " " + evaluator.getNom());
            dto.setEvaluatorEmail(evaluator.getEmail());
            dto.setEvaluatorRole(evaluator.getRole() != null ? evaluator.getRole().name() : null);
        }

        if (e.getChecklist() != null) {
            dto.setChecklistId(e.getChecklist().getId());
            dto.setChecklistTitre(e.getChecklist().getTitre());
        }

        Assignment assignment = e.getAssignment();
        if (assignment == null && e.getChecklist() != null) {
            assignment = e.getChecklist().getAssignment();
        }
        if (assignment != null) {
            dto.setAssignmentId(assignment.getId());
            if (assignment.getParcours() != null) {
                dto.setParcoursNom(assignment.getParcours().getNom());
            }
            User collab = assignment.getUser();
            if (collab != null) {
                dto.setCollaborateurId(collab.getId());
                dto.setCollaborateurNom(collab.getPrenom() + " " + collab.getNom());
                dto.setCollaborateurEmail(collab.getEmail());
            }
        }
        return dto;
    }
}

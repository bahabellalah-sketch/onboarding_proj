package com.onboarding.controller;

import com.onboarding.dto.AssignmentEvaluationSummaryDTO;
import com.onboarding.dto.CreateAssignmentEvaluationDTO;
import com.onboarding.dto.EvaluationDTO;
import com.onboarding.dto.PendingManagerEvaluationDTO;
import com.onboarding.entity.Evaluation;
import com.onboarding.entity.Role;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }
        }
        throw new RuntimeException("User not authenticated");
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createStepEvaluation(@RequestBody Map<String, Object> request) {
        try {
            Long checklistId = Long.valueOf(request.get("checklistId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : null;

            User current = getCurrentUser();
            if (!current.getId().equals(userId) && current.getRole() != Role.ADMINISTRATEUR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Action non autorisée"));
            }

            Evaluation evaluation = evaluationService.createStepEvaluation(checklistId, userId, rating, comment);
            return ResponseEntity.ok(evaluationService.toDto(evaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/assignment")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<?> createAssignmentEvaluation(@Valid @RequestBody CreateAssignmentEvaluationDTO dto) {
        try {
            Evaluation evaluation = evaluationService.createAssignmentEvaluation(getCurrentUser(), dto);
            return ResponseEntity.ok(evaluationService.toDto(evaluation));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assignments/{assignmentId}/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAssignmentSummary(@PathVariable Long assignmentId) {
        try {
            AssignmentEvaluationSummaryDTO summary = evaluationService.getAssignmentSummary(
                    getCurrentUser(), assignmentId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending/manager")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<PendingManagerEvaluationDTO>> getPendingForManager() {
        User current = getCurrentUser();
        if (current.getRole() == Role.ADMINISTRATEUR) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(evaluationService.getPendingForManager(current));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> getDashboard(@RequestParam(required = false) String type) {
        try {
            List<EvaluationDTO> list = evaluationService.getDashboardEvaluations(getCurrentUser(), type);
            return ResponseEntity.ok(list);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/checklist/{checklistId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getEvaluationByUserAndChecklist(
            @PathVariable Long userId,
            @PathVariable Long checklistId) {
        try {
            Evaluation evaluation = evaluationService.getEvaluationByUserAndChecklist(userId, checklistId);
            if (evaluation == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(evaluationService.toDto(evaluation));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/checklist/{checklistId}/can-evaluate/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> canUserEvaluate(
            @PathVariable Long checklistId,
            @PathVariable Long userId) {
        try {
            boolean canEvaluate = evaluationService.canUserEvaluateStep(userId, checklistId);
            return ResponseEntity.ok(Map.of("canEvaluate", canEvaluate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/checklist/{checklistId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> getEvaluationsByChecklist(@PathVariable Long checklistId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByChecklist(checklistId);
            return ResponseEntity.ok(evaluations.stream().map(evaluationService::toDto).toList());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/checklist/{checklistId}/average")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAverageRating(@PathVariable Long checklistId) {
        try {
            double averageRating = evaluationService.calculateAverageRating(checklistId);
            return ResponseEntity.ok(Map.of("averageRating", averageRating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

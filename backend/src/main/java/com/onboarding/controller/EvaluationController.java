package com.onboarding.controller;

import com.onboarding.entity.Evaluation;
import com.onboarding.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/evaluations")
@CrossOrigin(origins = "http://localhost:3000")
public class EvaluationController {
    
    @Autowired
    private EvaluationService evaluationService;
    
    /**
     * Create a new evaluation
     */
    @PostMapping
    public ResponseEntity<?> createEvaluation(@RequestBody Map<String, Object> request) {
        try {
            Long checklistId = Long.valueOf(request.get("checklistId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : null;
            
            Evaluation evaluation = evaluationService.createEvaluation(checklistId, userId, rating, comment);
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get evaluation by user and checklist item
     */
    @GetMapping("/user/{userId}/checklist/{checklistId}")
    public ResponseEntity<?> getEvaluationByUserAndChecklist(
            @PathVariable Long userId, 
            @PathVariable Long checklistId) {
        try {
            Evaluation evaluation = evaluationService.getEvaluationByUserAndChecklist(userId, checklistId);
            return ResponseEntity.ok(evaluation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Check if user can evaluate a checklist item
     */
    @GetMapping("/checklist/{checklistId}/can-evaluate/{userId}")
    public ResponseEntity<?> canUserEvaluate(
            @PathVariable Long checklistId, 
            @PathVariable Long userId) {
        try {
            boolean canEvaluate = evaluationService.canUserEvaluate(userId, checklistId);
            return ResponseEntity.ok(Map.of("canEvaluate", canEvaluate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all evaluations for a checklist item
     */
    @GetMapping("/checklist/{checklistId}")
    public ResponseEntity<?> getEvaluationsByChecklist(@PathVariable Long checklistId) {
        try {
            List<Evaluation> evaluations = evaluationService.getEvaluationsByChecklist(checklistId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get average rating for a checklist item
     */
    @GetMapping("/checklist/{checklistId}/average")
    public ResponseEntity<?> getAverageRating(@PathVariable Long checklistId) {
        try {
            double averageRating = evaluationService.calculateAverageRating(checklistId);
            return ResponseEntity.ok(Map.of("averageRating", averageRating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

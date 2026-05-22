package com.onboarding.service;

import com.onboarding.entity.Evaluation;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.User;
import com.onboarding.entity.StatutChecklist;
import com.onboarding.repository.EvaluationRepository;
import com.onboarding.repository.ChecklistRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private ChecklistRepository checklistRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new evaluation
     */
    public Evaluation createEvaluation(Long checklistId, Long userId, Integer rating, String comment) {
        // Find checklist
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        
        // Check if checklist is completed
        if (checklist.getStatut() != StatutChecklist.TERMINE) {
            throw new RuntimeException("Evaluation is only available after checklist completion");
        }
        
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user already evaluated
        if (evaluationRepository.existsByUserAndChecklist(user, checklist)) {
            throw new RuntimeException("User has already evaluated this checklist item");
        }
        
        // Create evaluation
        Evaluation evaluation = new Evaluation(checklist, user, rating, comment);
        Evaluation saved = evaluationRepository.save(evaluation);
        notificationService.sendEvaluationReceivedNotification(saved);
        return saved;
    }
    
    /**
     * Get evaluation by user and checklist
     */
    public Evaluation getEvaluationByUserAndChecklist(Long userId, Long checklistId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Checklist checklist = checklistRepository.findById(checklistId)
                    .orElseThrow(() -> new RuntimeException("Checklist not found"));
            
            return evaluationRepository.findByUserAndChecklist(user, checklist)
                    .orElse(null);
        } catch (Exception e) {
            System.out.println("DEBUG: Error in getEvaluationByUserAndChecklist: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Check if user can evaluate a checklist item
     */
    public boolean canUserEvaluate(Long userId, Long checklistId) {
        try {
            Checklist checklist = checklistRepository.findById(checklistId)
                    .orElseThrow(() -> new RuntimeException("Checklist not found"));
            
            // Check if checklist is completed
            if (checklist.getStatut() != StatutChecklist.TERMINE) {
                return false;
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user already evaluated
            return !evaluationRepository.existsByUserAndChecklist(user, checklist);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get all evaluations for a checklist
     */
    public List<Evaluation> getEvaluationsByChecklist(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        
        return evaluationRepository.findByChecklist(checklist);
    }
    
    /**
     * Calculate average rating for a checklist
     */
    public double calculateAverageRating(Long checklistId) {
        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        
        Double average = evaluationRepository.calculateAverageRatingByChecklist(checklist);
        return average != null ? average : 0.0;
    }
}

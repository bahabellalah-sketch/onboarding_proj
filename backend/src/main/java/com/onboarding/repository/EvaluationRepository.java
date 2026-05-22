package com.onboarding.repository;

import com.onboarding.entity.Evaluation;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    
    // Find evaluation by user and checklist
    Optional<Evaluation> findByUserAndChecklist(User user, Checklist checklist);
    
    // Check if user has already evaluated a checklist
    boolean existsByUserAndChecklist(User user, Checklist checklist);
    
    // Find all evaluations for a checklist
    List<Evaluation> findByChecklist(Checklist checklist);
    
    // Find all evaluations by a user
    List<Evaluation> findByUser(User user);
    
    // Count evaluations for a checklist
    long countByChecklist(Checklist checklist);
    
    // Calculate average rating for a checklist
    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.checklist = :checklist")
    Double calculateAverageRatingByChecklist(@Param("checklist") Checklist checklist);
}

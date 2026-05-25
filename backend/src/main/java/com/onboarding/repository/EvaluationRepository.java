package com.onboarding.repository;

import com.onboarding.entity.Assignment;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.Evaluation;
import com.onboarding.entity.EvaluationType;
import com.onboarding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByUserAndChecklist(User user, Checklist checklist);

    boolean existsByUserAndChecklist(User user, Checklist checklist);

    Optional<Evaluation> findByUserAndAssignmentAndEvaluationType(User user, Assignment assignment, EvaluationType type);

    boolean existsByUserAndAssignmentAndEvaluationType(User user, Assignment assignment, EvaluationType type);

    List<Evaluation> findByChecklist(Checklist checklist);

    List<Evaluation> findByAssignmentAndEvaluationType(Assignment assignment, EvaluationType type);

    List<Evaluation> findByAssignment(Assignment assignment);

    List<Evaluation> findByUser(User user);

    List<Evaluation> findByEvaluationTypeOrderByDateEvaluationDesc(EvaluationType type);

    @Query("SELECT e FROM Evaluation e ORDER BY e.dateEvaluation DESC")
    List<Evaluation> findAllOrderByDateDesc();

    long countByChecklist(Checklist checklist);

    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.checklist = :checklist")
    Double calculateAverageRatingByChecklist(@Param("checklist") Checklist checklist);

    @Query("SELECT AVG(e.rating) FROM Evaluation e WHERE e.assignment = :assignment AND e.evaluationType = com.onboarding.entity.EvaluationType.ETAPE")
    Double calculateAverageStepRatingByAssignment(@Param("assignment") Assignment assignment);

    @Query("SELECT COUNT(e) FROM Evaluation e WHERE e.assignment = :assignment AND e.evaluationType = com.onboarding.entity.EvaluationType.ETAPE")
    long countStepEvaluationsByAssignment(@Param("assignment") Assignment assignment);
}

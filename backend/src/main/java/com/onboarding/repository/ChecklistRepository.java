package com.onboarding.repository;

import com.onboarding.entity.Checklist;
import com.onboarding.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    
    // Trouver toutes les checklists pour une assignation
    List<Checklist> findByAssignment(Assignment assignment);
    
    // Trouver toutes les checklists pour une assignation par ID
    List<Checklist> findByAssignmentId(Long assignmentId);
    
    // Trouver une checklist spécifique par assignation et titre
    Optional<Checklist> findByAssignmentAndTitre(Assignment assignment, String titre);
    
    // Compter les checklists terminées pour une assignation
    @Query("SELECT COUNT(c) FROM Checklist c WHERE c.assignment.id = :assignmentId AND c.statut = 'TERMINE'")
    int countCompletedChecklists(@Param("assignmentId") Long assignmentId);
    
    // Compter le total des checklists pour une assignation
    @Query("SELECT COUNT(c) FROM Checklist c WHERE c.assignment.id = :assignmentId")
    int totalChecklists(@Param("assignmentId") Long assignmentId);
    
    // Trouver les checklists obligatoires non terminées
    @Query("SELECT c FROM Checklist c WHERE c.assignment.id = :assignmentId AND c.obligatoire = true AND c.statut != 'TERMINE'")
    List<Checklist> findMandatoryPendingChecklists(@Param("assignmentId") Long assignmentId);

    @Query("SELECT c FROM Checklist c WHERE c.etape.id = :etapeId")
    List<Checklist> findByEtapeId(@Param("etapeId") Long etapeId);
}

package com.onboarding.repository;

import com.onboarding.entity.Assignment;
import com.onboarding.entity.StatutAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    // Trouver les assignations par utilisateur
    List<Assignment> findByUserId(Long userId);
    
    // Trouver les assignations par plusieurs utilisateurs (pour les managers)
    List<Assignment> findByUserIdIn(List<Long> userIds);
    
    // Trouver les assignations par parcours
    List<Assignment> findByParcoursId(Long parcoursId);
    
    // Trouver une assignation spécifique (utilisateur + parcours)
    Optional<Assignment> findByUserIdAndParcoursId(Long userId, Long parcoursId);
    
    // Trouver les assignations par statut
    List<Assignment> findByStatut(StatutAssignment statut);
    
    // Trouver les assignations qui n'ont pas un certain statut
    List<Assignment> findByStatutNot(StatutAssignment statut);
    
    // Trouver les assignations en retard
    @Query("SELECT a FROM Assignment a WHERE a.dateFinPrevisionnelle < :currentDate AND a.statut NOT IN ('TERMINE', 'ANNULE')")
    List<Assignment> findAssignmentsEnRetard(@Param("currentDate") LocalDate currentDate);
    
    // Trouver les assignations se terminant bientôt (dans les 7 jours)
    @Query("SELECT a FROM Assignment a WHERE a.dateFinPrevisionnelle BETWEEN :currentDate AND :endDate AND a.statut NOT IN ('TERMINE', 'ANNULE')")
    List<Assignment> findAssignmentsProchesEcheance(@Param("currentDate") LocalDate currentDate, @Param("endDate") LocalDate endDate);
    
    // Compter les assignations par utilisateur
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.user.id = :userId")
    Long countAssignmentsByUser(@Param("userId") Long userId);
    
    // Compter les assignations terminées par utilisateur
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.user.id = :userId AND a.statut = 'TERMINE'")
    Long countAssignmentsTerminesByUser(@Param("userId") Long userId);
    
    // Statistiques par parcours
    @Query("SELECT a.statut, COUNT(a) FROM Assignment a WHERE a.parcours.id = :parcoursId GROUP BY a.statut")
    List<Object[]> getStatistiquesByParcours(@Param("parcoursId") Long parcoursId);
    
    // Assignations actives d'un utilisateur
    @Query("SELECT a FROM Assignment a WHERE a.user.id = :userId AND a.statut IN ('EN_COURS', 'EN_PAUSE')")
    List<Assignment> findAssignmentsActivesByUser(@Param("userId") Long userId);
    
    // Vérifier si un utilisateur a une assignation active
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Assignment a WHERE a.user.id = :userId AND a.statut IN ('EN_COURS', 'EN_PAUSE')")
    boolean hasActiveAssignment(@Param("userId") Long userId);
}

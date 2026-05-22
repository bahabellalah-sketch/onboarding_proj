package com.onboarding.repository;

import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutParcours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingParcoursRepository extends JpaRepository<OnboardingParcours, Long> {
    
    List<OnboardingParcours> findByStatut(StatutParcours statut);
    
    List<OnboardingParcours> findByCategorieCible(String categorieCible);
    
    List<OnboardingParcours> findByDepartementCible(String departementCible);
    
    Optional<OnboardingParcours> findById(Long id);
    
    void deleteById(Long id);
}

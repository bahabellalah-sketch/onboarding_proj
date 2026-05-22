package com.onboarding.repository;

import com.onboarding.entity.Etape;
import com.onboarding.entity.TypeEtape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtapeRepository extends JpaRepository<Etape, Long> {
    
    List<Etape> findByParcours_Id(Long parcoursId);
    
    List<Etape> findByParcours_IdOrderByOrdreExecution(Long parcoursId);
    
    List<Etape> findByType(TypeEtape type);
    
    Optional<Etape> findById(Long id);
    
    void deleteById(Long id);
    
    void deleteByParcours_Id(Long parcoursId);
}

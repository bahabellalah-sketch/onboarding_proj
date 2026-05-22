package com.onboarding.service;

import com.onboarding.entity.Etape;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutParcours;
import com.onboarding.repository.OnboardingParcoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OnboardingParcoursService {
    
    @Autowired
    private OnboardingParcoursRepository parcoursRepository;
    
    @Autowired
    private ParcoursEtapeAiGeneratorService parcoursEtapeAiGeneratorService;
    
    public List<OnboardingParcours> getAllParcours() {
        return parcoursRepository.findAll();
    }
    
    public Optional<OnboardingParcours> getParcoursById(Long id) {
        return parcoursRepository.findById(id);
    }
    
    public OnboardingParcours saveParcours(OnboardingParcours parcours) {
        return parcoursRepository.save(parcours);
    }
    
    public OnboardingParcours updateParcours(Long id, OnboardingParcours parcoursDetails) {
        Optional<OnboardingParcours> existingParcours = parcoursRepository.findById(id);
        if (existingParcours.isPresent()) {
            OnboardingParcours parcours = existingParcours.get();
            parcours.setNom(parcoursDetails.getNom());
            parcours.setDescription(parcoursDetails.getDescription());
            parcours.setCategorieCible(parcoursDetails.getCategorieCible());
            parcours.setDepartementCible(parcoursDetails.getDepartementCible());
            parcours.setDureeGlobaleEstimee(parcoursDetails.getDureeGlobaleEstimee());
            parcours.setDeadlineGlobaleParDefaut(parcoursDetails.getDeadlineGlobaleParDefaut());
            parcours.setStatut(parcoursDetails.getStatut());
            return parcoursRepository.save(parcours);
        }
        return null;
    }
    
    public void deleteParcours(Long id) {
        parcoursRepository.deleteById(id);
    }
    
    public List<OnboardingParcours> getParcoursByStatut(StatutParcours statut) {
        return parcoursRepository.findByStatut(statut);
    }
    
    public List<OnboardingParcours> getParcoursByCategorie(String categorie) {
        return parcoursRepository.findByCategorieCible(categorie);
    }
    
    public List<OnboardingParcours> getParcoursByDepartement(String departement) {
        return parcoursRepository.findByDepartementCible(departement);
    }
    
    public boolean existsById(Long id) {
        return parcoursRepository.findById(id).isPresent();
    }

    public List<Etape> generateEtapesWithAI(Long parcoursId) {
        return parcoursEtapeAiGeneratorService.generateForParcours(parcoursId);
    }
}

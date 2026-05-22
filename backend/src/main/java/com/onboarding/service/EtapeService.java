package com.onboarding.service;

import com.onboarding.entity.Etape;
import com.onboarding.entity.TypeEtape;
import com.onboarding.repository.EtapeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EtapeService {
    
    @Autowired
    private EtapeRepository etapeRepository;
    
    public List<Etape> getAllEtapes() {
        return etapeRepository.findAll();
    }
    
    public List<Etape> getEtapesByParcoursId(Long parcoursId) {
        return etapeRepository.findByParcours_IdOrderByOrdreExecution(parcoursId);
    }
    
    public Optional<Etape> getEtapeById(Long id) {
        return etapeRepository.findById(id);
    }
    
    public Etape saveEtape(Etape etape) {
        return etapeRepository.save(etape);
    }
    
    public Etape updateEtape(Long id, Etape etapeDetails) {
        Optional<Etape> existingEtape = etapeRepository.findById(id);
        if (existingEtape.isPresent()) {
            Etape etape = existingEtape.get();
            etape.setNom(etapeDetails.getNom());
            etape.setType(etapeDetails.getType());
            etape.setDureeEstimee(etapeDetails.getDureeEstimee());
            etape.setOrdreExecution(etapeDetails.getOrdreExecution());
            return etapeRepository.save(etape);
        }
        return null;
    }
    
    public void deleteEtape(Long id) {
        etapeRepository.deleteById(id);
    }
    
    public void deleteEtapesByParcoursId(Long parcoursId) {
        etapeRepository.deleteByParcours_Id(parcoursId);
    }
    
    public List<Etape> getEtapesByType(TypeEtape type) {
        return etapeRepository.findByType(type);
    }
    
    public boolean existsById(Long id) {
        return etapeRepository.findById(id).isPresent();
    }
}

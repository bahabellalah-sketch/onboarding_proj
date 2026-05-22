package com.onboarding.controller;

import com.onboarding.dto.EtapeDTO;
import com.onboarding.entity.Etape;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.TypeEtape;
import com.onboarding.service.EtapeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/etapes")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class EtapeController {
    
    @Autowired
    private EtapeService etapeService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<Etape>> getAllEtapes() {
        try {
            List<Etape> etapes = etapeService.getAllEtapes();
            return ResponseEntity.ok(etapes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Etape> getEtapeById(@PathVariable Long id) {
        try {
            Optional<Etape> etape = etapeService.getEtapeById(id);
            if (etape.isPresent()) {
                return ResponseEntity.ok(etape.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/parcours/{parcoursId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<Etape>> getEtapesByParcoursId(@PathVariable Long parcoursId) {
        try {
            List<Etape> etapes = etapeService.getEtapesByParcoursId(parcoursId);
            return ResponseEntity.ok(etapes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        System.out.println("=== DEBUG: Etape test endpoint called ===");
        return ResponseEntity.ok("Etape controller is working");
    }

    @PostMapping("/debug")
    public ResponseEntity<String> debugEndpoint(@RequestBody String body) {
        System.out.println("=== DEBUG: POST debug endpoint called ===");
        System.out.println("Request body: " + body);
        return ResponseEntity.ok("Debug endpoint received: " + body);
    }

    @PostMapping("/simple")
    public ResponseEntity<String> createEtapeSimple(@RequestBody String body) {
        System.out.println("=== DEBUG: createEtapeSimple method called ===");
        System.out.println("Request body: " + body);
        try {
            // Use Spring's configured ObjectMapper
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            EtapeDTO etapeDTO = mapper.readValue(body, EtapeDTO.class);
            System.out.println("=== DEBUG: Successfully parsed EtapeDTO ===");
            System.out.println("Nom: " + etapeDTO.getNom());
            System.out.println("Type: " + etapeDTO.getType());
            System.out.println("ParcoursId: " + etapeDTO.getParcoursId());
            return ResponseEntity.ok("EtapeDTO parsed successfully: " + etapeDTO.getNom());
        } catch (Exception e) {
            System.out.println("=== ERROR: Failed to parse EtapeDTO ===");
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error parsing EtapeDTO: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Etape> createEtape(@RequestBody EtapeDTO etapeDTO) {
        try {
            Etape etape = convertToEntity(etapeDTO);
            Etape savedEtape = etapeService.saveEtape(etape);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEtape);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Etape> updateEtape(@PathVariable Long id, @RequestBody EtapeDTO etapeDTO) {
        System.out.println("=== DEBUG: updateEtape method called ===");
        try {
            System.out.println("=== DEBUG: Received EtapeDTO for update ===");
            System.out.println("ID: " + id);
            System.out.println("Nom: " + etapeDTO.getNom());
            System.out.println("Type: " + etapeDTO.getType());
            System.out.println("ParcoursId: " + etapeDTO.getParcoursId());
            
            Etape etapeDetails = convertToEntity(etapeDTO);
            Etape updatedEtape = etapeService.updateEtape(id, etapeDetails);
            if (updatedEtape != null) {
                return ResponseEntity.ok(updatedEtape);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("=== ERROR: Exception in updateEtape ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Void> deleteEtape(@PathVariable Long id) {
        try {
            if (etapeService.existsById(id)) {
                etapeService.deleteEtape(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<Etape>> getEtapesByType(@PathVariable TypeEtape type) {
        try {
            List<Etape> etapes = etapeService.getEtapesByType(type);
            return ResponseEntity.ok(etapes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    private Etape convertToEntity(EtapeDTO dto) {
        Etape etape = new Etape();
        etape.setNom(dto.getNom());
        etape.setType(dto.getType());
        etape.setDureeEstimee(dto.getDureeEstimee());
        etape.setOrdreExecution(dto.getOrdreExecution());
        etape.setRequiertDocument(dto.getRequiertDocument());
        
        // Set parcours relationship
        if (dto.getParcoursId() != null) {
            OnboardingParcours parcours = new OnboardingParcours();
            parcours.setId(dto.getParcoursId());
            etape.setParcours(parcours);
        }
        
        return etape;
    }
    

}

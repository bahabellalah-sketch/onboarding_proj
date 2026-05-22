package com.onboarding.controller;

import com.onboarding.dto.AiGeneratedParcoursResponse;
import com.onboarding.dto.AiParcoursRequest;
import com.onboarding.dto.OnboardingParcoursDTO;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutParcours;
import com.onboarding.service.AiFullParcoursGeneratorService;
import com.onboarding.service.OnboardingParcoursService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/parcours")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OnboardingParcoursController {
    
    @Autowired
    private OnboardingParcoursService parcoursService;

    @Autowired
    private AiFullParcoursGeneratorService aiFullParcoursGeneratorService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<List<OnboardingParcours>> getAllParcours() {
        try {
            List<OnboardingParcours> parcours = parcoursService.getAllParcours();
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<OnboardingParcours> getParcoursById(@PathVariable Long id) {
        try {
            Optional<OnboardingParcours> parcours = parcoursService.getParcoursById(id);
            if (parcours.isPresent()) {
                return ResponseEntity.ok(parcours.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<OnboardingParcours> createParcours(@Valid @RequestBody OnboardingParcoursDTO parcoursDTO) {
        try {
            OnboardingParcours parcours = convertToEntity(parcoursDTO);
            OnboardingParcours savedParcours = parcoursService.saveParcours(parcours);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedParcours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<OnboardingParcours> updateParcours(@PathVariable Long id, @Valid @RequestBody OnboardingParcoursDTO parcoursDTO) {
        try {
            OnboardingParcours parcoursDetails = convertToEntity(parcoursDTO);
            OnboardingParcours updatedParcours = parcoursService.updateParcours(id, parcoursDetails);
            if (updatedParcours != null) {
                return ResponseEntity.ok(updatedParcours);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Void> deleteParcours(@PathVariable Long id) {
        try {
            if (parcoursService.existsById(id)) {
                parcoursService.deleteParcours(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<OnboardingParcours>> getParcoursByStatut(@PathVariable StatutParcours statut) {
        try {
            List<OnboardingParcours> parcours = parcoursService.getParcoursByStatut(statut);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/categorie/{categorie}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<OnboardingParcours>> getParcoursByCategorie(@PathVariable String categorie) {
        try {
            List<OnboardingParcours> parcours = parcoursService.getParcoursByCategorie(categorie);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @GetMapping("/departement/{departement}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<OnboardingParcours>> getParcoursByDepartement(@PathVariable String departement) {
        try {
            List<OnboardingParcours> parcours = parcoursService.getParcoursByDepartement(departement);
            return ResponseEntity.ok(parcours);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/{id}/generate-etapes-ai")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> generateEtapesWithAI(@PathVariable Long id) {
        try {
            System.out.println("Generating etapes with AI for parcours ID: " + id);
            List<?> generatedEtapes = parcoursService.generateEtapesWithAI(id);
            return ResponseEntity.ok(generatedEtapes);
        } catch (Exception e) {
            System.err.println("Error generating etapes with AI: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating etapes with AI: " + e.getMessage());
        }
    }

    /**
     * AI-powered endpoint: generates a complete onboarding program (parcours + tasks)
     * from a simple text description using Mistral/Ollama.
     * 
     * Example request body:
     * {
     *   "prompt": "Create an onboarding for a Java backend developer using Spring Boot"
     * }
     */
    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> generateParcoursWithAI(@RequestBody AiParcoursRequest request) {
        try {
            if (request.getPrompt() == null || request.getPrompt().isBlank()) {
                return ResponseEntity.badRequest().body("Le prompt ne peut pas être vide");
            }

            System.out.println("AI: Generating full parcours from prompt: " + request.getPrompt());
            AiGeneratedParcoursResponse generated = aiFullParcoursGeneratorService.generate(request.getPrompt());
            System.out.println("AI: Successfully generated parcours ID: " + generated.getId()
                    + " - " + generated.getNom() + " (" + generated.getEtapeCount() + " etapes)");

            return ResponseEntity.status(HttpStatus.CREATED).body(generated);
        } catch (Exception e) {
            System.err.println("AI: Error generating full parcours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération du parcours: " + e.getMessage());
        }
    }
    
    private OnboardingParcours convertToEntity(OnboardingParcoursDTO dto) {
        OnboardingParcours parcours = new OnboardingParcours();
        parcours.setNom(dto.getNom());
        parcours.setDescription(dto.getDescription());
        parcours.setCategorieCible(dto.getCategorieCible());
        parcours.setDepartementCible(dto.getDepartementCible());
        parcours.setDureeGlobaleEstimee(dto.getDureeGlobaleEstimee());
        parcours.setDeadlineGlobaleParDefaut(dto.getDeadlineGlobaleParDefaut());
        parcours.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutParcours.ACTIF);
        return parcours;
    }
}

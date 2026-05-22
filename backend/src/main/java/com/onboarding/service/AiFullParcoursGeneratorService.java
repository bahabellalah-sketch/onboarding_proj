package com.onboarding.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onboarding.dto.AiEtapeDTO;
import com.onboarding.dto.AiFullParcoursDTO;
import com.onboarding.dto.AiGeneratedParcoursResponse;
import com.onboarding.entity.Etape;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutParcours;
import com.onboarding.entity.TypeEtape;
import com.onboarding.repository.EtapeRepository;
import com.onboarding.repository.OnboardingParcoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service that uses the AI to generate a complete onboarding program (parcours + tasks)
 * from a single user prompt, then saves everything to the database.
 */
@Service
@Transactional
public class AiFullParcoursGeneratorService {

    @Autowired
    private AiClient aiClient;

    @Autowired
    private OnboardingParcoursRepository parcoursRepository;

    @Autowired
    private EtapeRepository etapeRepository;

    private final ObjectMapper objectMapper;

    public AiFullParcoursGeneratorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Generate a complete onboarding program (parcours + tasks) from a user prompt.
     *
     * @param prompt The user's description of the onboarding they need
     * @return The saved OnboardingParcours with tasks attached
     */
    public AiGeneratedParcoursResponse generate(String prompt) {
        System.out.println("AI: Generating full parcours from prompt: " + prompt);

        // Step 1: Call AI
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(prompt);

        String response = aiClient.chat(systemPrompt, userPrompt);
        System.out.println("AI: Raw response received (" + response.length() + " chars)");

        // Step 2: Clean response — extract JSON from markdown fences or raw text
        String cleaned = response.trim();

        // Remove markdown code fences (```json ... ``` or just ``` ... ```)
        cleaned = cleaned.replaceAll("(?s)```(?:json)?\\s*", "").trim();

        // If the response doesn't start with '{', try to find JSON object
        if (!cleaned.startsWith("{")) {
            int jsonStart = cleaned.indexOf('{');
            int jsonEnd = cleaned.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
            }
        }

        System.out.println("AI: Cleaned response (" + cleaned.length() + " chars): " + cleaned.substring(0, Math.min(150, cleaned.length())));

        // Step 3: Parse the structured JSON response
        AiFullParcoursDTO dto;
        try {
            dto = objectMapper.readValue(cleaned, AiFullParcoursDTO.class);
        } catch (Exception e) {
            String excerpt = cleaned.length() > 500 ? cleaned.substring(0, 500) + "..." : cleaned;
            throw new RuntimeException("Erreur lors du parsing de la réponse de l'IA: " + e.getMessage() + 
                ". Les 500 premiers caractères de la réponse: " + excerpt, e);
        }

        // Validate
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new RuntimeException("L'IA n'a pas généré de nom pour le parcours");
        }
        if (dto.getEtapes() == null || dto.getEtapes().isEmpty()) {
            throw new RuntimeException("L'IA n'a pas généré d'étapes pour le parcours");
        }

        System.out.println("AI: Parsed parcours \"" + dto.getNom() + "\" with " + dto.getEtapes().size() + " tasks");

        // Step 4: Save the parcours entity
        OnboardingParcours parcours = new OnboardingParcours();
        parcours.setNom(dto.getNom());
        parcours.setDescription(dto.getDescription());
        parcours.setCategorieCible(dto.getCategorieCible() != null ? dto.getCategorieCible() : "Général");
        parcours.setDepartementCible(dto.getDepartementCible() != null ? dto.getDepartementCible() : "Général");
        parcours.setDureeGlobaleEstimee(dto.getDureeGlobaleEstimee());
        int deadline = dto.getDureeGlobaleEstimee() != null
                ? Math.max(dto.getDureeGlobaleEstimee(), 30)
                : 45;
        parcours.setDeadlineGlobaleParDefaut(deadline);
        parcours.setStatut(StatutParcours.ACTIF);

        OnboardingParcours savedParcours = parcoursRepository.save(parcours);
        System.out.println("AI: Saved parcours with ID: " + savedParcours.getId());

        // Step 5: Save all tasks linked to the parcours
        List<Etape> savedEtapes = new ArrayList<>();
        int ordre = 1;
        for (AiEtapeDTO etapeDTO : dto.getEtapes()) {
            Etape etape = convertToEntity(etapeDTO, ordre++, savedParcours);
            savedEtapes.add(etapeRepository.save(etape));
        }

        System.out.println("AI: Saved " + savedEtapes.size() + " tasks for parcours ID: " + savedParcours.getId());

        OnboardingParcours persisted = parcoursRepository.findById(savedParcours.getId()).orElse(savedParcours);
        return AiGeneratedParcoursResponse.from(persisted, savedEtapes);
    }

    /**
     * Build the system prompt instructing the AI how to format its response.
     */
    private String buildSystemPrompt() {
        return """
Tu es un expert en création de parcours d'onboarding en entreprise.

Tu dois générer un parcours d'onboarding COMPLET avec ses étapes.

Respecte STRICTEMENT ce format JSON. Ne retourne RIEN d'autre que le JSON.

{
  "nom": "Nom du parcours (court, clair)",
  "description": "Description détaillée du parcours (2-3 phrases)",
  "categorieCible": "Catégorie métier (ex: Technique, RH, Commercial, etc.)",
  "departementCible": "Département concerné",
  "dureeGlobaleEstimee": nombre total de jours estimé (entier),
  "etapes": [
    {
      "nom": "Titre de l'étape",
      "description": "Description de ce qu'il faut faire",
      "type": "TECHNIQUE ou HUMAIN ou ADMINISTRATIF",
      "dureeEstimee": nombre de jours (entier 1-10),
      "requiertDocument": true ou false,
      "resourceLinks": "URL ou chaîne vide"
    }
  ]
}

RÈGLES IMPORTANTES:
- Génère entre 6 et 12 étapes variées
- La première étape doit être une étape d'accueil/présentation
- La dernière étape doit être une étape de clôture/bilan
- Inclus un mélange de types TECHNIQUE, HUMAIN et ADMINISTRATIF
- Retourne UNIQUEMENT le JSON, sans aucun texte avant ou après
""";
    }

    /**
     * Build the user prompt with their specific request.
     */
    private String buildUserPrompt(String userPrompt) {
        return """
Crée un parcours d'onboarding complet basé sur la demande suivante :

---
%s
---

Génère le JSON du parcours avec toutes ses étapes maintenant.
""".formatted(userPrompt);
    }

    /**
     * Convert AI DTO to Etape entity.
     */
    private Etape convertToEntity(AiEtapeDTO dto, int ordre, OnboardingParcours parcours) {
        Etape etape = new Etape();
        etape.setNom(dto.getNom() != null && !dto.getNom().isBlank()
                ? dto.getNom() : "Étape " + ordre);
        etape.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
        etape.setType(parseType(dto.getType()));
        etape.setOrdreExecution(ordre);
        etape.setDureeEstimee(Math.max(1, Math.min(10, dto.getDureeEstimee())));
        etape.setRequiertDocument(dto.isRequiertDocument());
        if (dto.getResourceLinks() != null && !dto.getResourceLinks().isBlank()) {
            etape.setResourceLinks(dto.getResourceLinks());
        }
        etape.setParcours(parcours);
        return etape;
    }

    /**
     * Parse the type string, defaulting to TECHNIQUE if unknown.
     */
    private TypeEtape parseType(String type) {
        if (type == null) return TypeEtape.TECHNIQUE;
        try {
            return TypeEtape.valueOf(type.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return TypeEtape.TECHNIQUE;
        }
    }
}
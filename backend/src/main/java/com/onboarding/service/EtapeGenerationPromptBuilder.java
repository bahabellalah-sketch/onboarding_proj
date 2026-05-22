package com.onboarding.service;

import com.onboarding.entity.OnboardingParcours;
import org.springframework.stereotype.Component;

/**
 * Builds structured prompts for the AI to generate onboarding tasks.
 * Uses few-shot examples and detailed instructions to ensure high-quality,
 * varied results.
 */
@Component
public class EtapeGenerationPromptBuilder {

    /**
     * The system prompt that sets the AI's role and output format.
     */
    public String buildSystemPrompt() {
        return """
You are an expert HR onboarding specialist for a company. Your role is to create personalized, 
detailed onboarding task lists for new employees based on their specific onboarding program context.

RULES:
1. Generate between 6 and 12 tasks (étapes).
2. Each task must be a SINGLE JSON object with these exact fields:
   - "nom": short clear title (2-8 words, in French)
   - "description": 1-2 sentence description of what to do (in French)
   - "type": one of ["TECHNIQUE", "HUMAIN", "ADMINISTRATIF"]
   - "dureeEstimee": estimated days to complete (integer 1-10)
   - "requiertDocument": true/false (true if a document needs to be submitted/signed)
   - "resourceLinks": optional URL string or empty string "" if none
3. Return ONLY a valid JSON array. No markdown, no code fences, no extra text.
4. Vary the tasks each time — do not copy the examples verbatim.
5. Make tasks SPECIFIC to the given context (parcours name, description, category, department).
6. Include a mix of TECHNIQUE, HUMAIN, and ADMINISTRATIF tasks.
7. The first task should be an "Accueil / Welcome" type introduction.
8. The last task should be a "Clôture / Closing" type review.

EXAMPLES of good tasks (for reference only — adapt to the actual context):
[
  {
    "nom": "Accueil — Parcours Développeur Java",
    "description": "Session d'intégration avec le manager : présentation de l'équipe, objectifs du parcours et livrables attendus.",
    "type": "ADMINISTRATIF",
    "dureeEstimee": 1,
    "requiertDocument": false,
    "resourceLinks": ""
  },
  {
    "nom": "Formation Spring Boot & API REST",
    "description": "Suivre le tutoriel Spring Boot officiel et créer une première API REST simple avec JPA et H2.",
    "type": "TECHNIQUE",
    "dureeEstimee": 5,
    "requiertDocument": false,
    "resourceLinks": "https://spring.io/guides/gs/spring-boot/"
  },
  {
    "nom": "Signature charte informatique",
    "description": "Lire, signer et retourner la charte d'utilisation des outils informatiques et des données.",
    "type": "ADMINISTRATIF",
    "dureeEstimee": 1,
    "requiertDocument": true,
    "resourceLinks": ""
  },
  {
    "nom": "Présentation équipe et rituels agiles",
    "description": "Participer aux daily meetings, comprendre le workflow Jira et rencontrer les référents techniques.",
    "type": "HUMAIN",
    "dureeEstimee": 2,
    "requiertDocument": false,
    "resourceLinks": ""
  }
]
""";
    }

    /**
     * Build the user prompt with the specific parcours context.
     */
    public String buildUserPrompt(OnboardingParcours parcours) {
        String nom = parcours.getNom() != null ? parcours.getNom() : "Non défini";
        String description = parcours.getDescription() != null ? parcours.getDescription() : "Non définie";
        String categorie = parcours.getCategorieCible() != null ? parcours.getCategorieCible() : "Non spécifiée";
        String departement = parcours.getDepartementCible() != null ? parcours.getDepartementCible() : "Non spécifié";

        return """
Génère une liste d'étapes d'onboarding personnalisée pour le parcours suivant :

Nom du parcours : %s
Description : %s
Catégorie cible : %s
Département cible : %s

Important : adapte les étapes à ce contexte précis. Ne copie pas les exemples génériques.
Assure-toi d'avoir entre 6 et 12 étapes variées. Retourne UNIQUEMENT le JSON, sans aucun texte supplémentaire.
""".formatted(nom, description, categorie, departement);
    }
}
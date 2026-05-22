package com.onboarding.service;

import com.onboarding.dto.AiEtapeDTO;
import com.onboarding.entity.Etape;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.TypeEtape;
import com.onboarding.repository.EtapeRepository;
import com.onboarding.repository.OnboardingParcoursRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Génération intelligente d'étapes à partir du contexte complet du parcours.
 * 
 * Primary mode: Uses a real AI LLM (OpenAI-compatible API) to generate unique,
 * context-aware tasks. Falls back to the legacy rule-based system if the AI
 * is unavailable or returns an invalid response.
 */
@Service
public class ParcoursEtapeAiGeneratorService {

    @Autowired
    private OnboardingParcoursRepository parcoursRepository;

    @Autowired
    private EtapeRepository etapeRepository;

    @Autowired(required = false)
    private AiClient aiClient;

    @Autowired(required = false)
    private EtapeGenerationPromptBuilder promptBuilder;

    private final ObjectMapper objectMapper;

    public ParcoursEtapeAiGeneratorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Generate tasks for a given parcours.
     * Tries AI first, falls back to legacy rule-based system on failure.
     */
    public List<Etape> generateForParcours(Long parcoursId) {
        OnboardingParcours parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours not found with ID: " + parcoursId));

        // Delete existing tasks for this parcours
        etapeRepository.deleteByParcours_Id(parcoursId);

        // Try AI generation first
        if (aiClient != null && promptBuilder != null) {
            try {
                System.out.println("AI: Attempting AI generation for parcours: " + parcours.getNom());
                List<Etape> aiEtapes = generateWithAi(parcours);
                if (aiEtapes != null && !aiEtapes.isEmpty()) {
                    return saveAll(aiEtapes);
                }
            } catch (Exception e) {
                System.out.println("AI: Generation failed, falling back to legacy system. Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Fallback: use legacy rule-based system
        System.out.println("AI: Using legacy fallback generation for parcours: " + parcours.getNom());
        return generateWithLegacyFallback(parcours);
    }

    /**
     * Generate tasks using the AI LLM.
     */
    private List<Etape> generateWithAi(OnboardingParcours parcours) throws Exception {
        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(parcours);

        System.out.println("AI: Calling AI API...");
        String response = aiClient.chat(systemPrompt, userPrompt);
        System.out.println("AI: Raw response received (" + response.length() + " chars)");

        // Clean response: remove markdown code fences if present
        String cleaned = response;
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("```(?:json)?", "").trim();
        }

        // Parse JSON array
        List<AiEtapeDTO> dtos = objectMapper.readValue(cleaned, new TypeReference<List<AiEtapeDTO>>() {});

        if (dtos.isEmpty()) {
            throw new RuntimeException("AI returned empty task list");
        }

        System.out.println("AI: Parsed " + dtos.size() + " tasks from AI response");

        List<Etape> etapes = new ArrayList<>();
        int ordre = 1;
        for (AiEtapeDTO dto : dtos) {
            etapes.add(convertToEntity(dto, ordre++, parcours));
        }

        return etapes;
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

    /**
     * Save all generated entities.
     */
    private List<Etape> saveAll(List<Etape> etapes) {
        List<Etape> saved = new ArrayList<>();
        for (Etape e : etapes) {
            saved.add(etapeRepository.save(e));
        }
        System.out.println("AI: Saved " + saved.size() + " etapes");
        return saved;
    }

    // =====================
    // LEGACY FALLBACK SYSTEM
    // =====================

    private List<Etape> generateWithLegacyFallback(OnboardingParcours parcours) {
        String ctx = normalizeForMatch(join(
                parcours.getNom(),
                parcours.getDescription(),
                parcours.getCategorieCible(),
                parcours.getDepartementCible()
        ));

        ParcoursTrack track = inferTrack(ctx);
        List<Etape> generated = new ArrayList<>();
        int ordre = 1;

        String parcoursLabel = parcours.getNom() != null ? parcours.getNom() : "votre parcours";

        generated.add(step(
                "Accueil — " + parcoursLabel,
                "Session d'intégration dédiée au parcours « " + parcoursLabel + " » : objectifs, équipe, manager et livrables attendus.",
                TypeEtape.ADMINISTRATIF, ordre++, 1, false, null, parcours));

        generated.add(step(
                "Documentation & conformité",
                "Politiques internes, sécurité des données, charte IT et signatures des documents obligatoires.",
                TypeEtape.ADMINISTRATIF, ordre++, 2, true,
                "https://www.youtube.com/watch?v=inWWhr5tnEA", parcours));

        generated.add(step(
                "Accès outils & environnement",
                "Mail, SSO, Slack/Teams, Jira, VPN/MFA et accès aux environnements selon le rôle.",
                TypeEtape.TECHNIQUE, ordre++, 2, false, null, parcours));

        switch (track) {
            case HR -> ordre = addHrTrack(generated, parcours, ordre);
            case FULLSTACK -> ordre = addFullstackTrack(generated, parcours, ordre);
            case BACKEND_JAVA -> ordre = addBackendJavaTrack(generated, parcours, ordre);
            case BACKEND_NODE -> ordre = addBackendNodeTrack(generated, parcours, ordre);
            case BACKEND_PYTHON -> ordre = addBackendPythonTrack(generated, parcours, ordre);
            case FRONTEND_WEB -> ordre = addFrontendTrack(generated, parcours, ordre);
            case MOBILE -> ordre = addMobileTrack(generated, parcours, ordre);
            case DATA_SCIENCE -> ordre = addDataScienceTrack(generated, parcours, ordre);
            case QA -> ordre = addQaTrack(generated, parcours, ordre);
            case DEVOPS -> ordre = addDevOpsTrack(generated, parcours, ordre);
            case CYBERSEC -> ordre = addCybersecTrack(generated, parcours, ordre);
            case GENERIC_TECH -> ordre = addGenericTechTrack(generated, parcours, ordre);
            default -> ordre = addNonTechTrack(generated, parcours, ordre);
        }

        generated.add(step(
                "Objectifs 30-60-90 jours",
                "Définition des KPIs et plan de montée en compétence avec le manager.",
                TypeEtape.ADMINISTRATIF, ordre++, 2, true, null, parcours));

        generated.add(step(
                "Clôture du parcours « " + parcoursLabel + " »",
                "Bilan d'intégration, feedback 360° léger et validation des jalons du parcours.",
                TypeEtape.ADMINISTRATIF, ordre, 1, false, null, parcours));

        return saveAll(generated);
    }

    // Legacy enum
    private enum ParcoursTrack {
        HR, FULLSTACK, BACKEND_JAVA, BACKEND_NODE, BACKEND_PYTHON, FRONTEND_WEB, MOBILE,
        DATA_SCIENCE, QA, DEVOPS, CYBERSEC, GENERIC_TECH, GENERIC_OTHER
    }

    private ParcoursTrack inferTrack(String ctx) {
        Map<ParcoursTrack, Integer> scores = new EnumMap<>(ParcoursTrack.class);
        for (ParcoursTrack t : ParcoursTrack.values()) {
            scores.put(t, 0);
        }

        scoreHr(ctx, scores);
        scoreFullstack(ctx, scores);
        scoreBackendJava(ctx, scores);
        scoreBackendNode(ctx, scores);
        scoreBackendPython(ctx, scores);
        scoreFrontend(ctx, scores);
        scoreMobile(ctx, scores);
        scoreDataScience(ctx, scores);
        scoreQa(ctx, scores);
        scoreDevOps(ctx, scores);
        scoreCybersec(ctx, scores);
        scoreGenericTech(ctx, scores);

        ParcoursTrack best = ParcoursTrack.GENERIC_OTHER;
        int bestScore = 0;
        for (Map.Entry<ParcoursTrack, Integer> e : scores.entrySet()) {
            if (e.getKey() == ParcoursTrack.GENERIC_OTHER) continue;
            if (e.getValue() > bestScore) {
                bestScore = e.getValue();
                best = e.getKey();
            }
        }
        if (bestScore == 0 && scores.get(ParcoursTrack.GENERIC_TECH) > 0) {
            return ParcoursTrack.GENERIC_TECH;
        }
        return best;
    }

    private void scoreHr(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "hr ", " rh", " rh ", "human resource", "talent", "recrutement", "people ops", "sirh"))
            s.merge(ParcoursTrack.HR, 10, Integer::sum);
    }

    private void scoreFullstack(String ctx, Map<ParcoursTrack, Integer> s) {
        boolean full = containsAny(ctx, "fullstack", "full-stack", "full stack", "full stack developer");
        boolean back = containsAny(ctx, "backend", "back-end", "spring", "java");
        boolean front = containsAny(ctx, "frontend", "front-end", "react", "angular", "vue");
        if (full) s.merge(ParcoursTrack.FULLSTACK, 12, Integer::sum);
        if (back && front) s.merge(ParcoursTrack.FULLSTACK, 8, Integer::sum);
    }

    private void scoreBackendJava(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "spring", "springboot", "spring boot", "jpa", "hibernate", "maven", "gradle", "microservice"))
            s.merge(ParcoursTrack.BACKEND_JAVA, 8, Integer::sum);
        if (containsAny(ctx, "backend", "back-end", "developpeur back", "ingenieur back", "server-side", "api rest"))
            s.merge(ParcoursTrack.BACKEND_JAVA, 5, Integer::sum);
        if (containsAny(ctx, "java") && containsAny(ctx, "backend", "api", "serveur", "spring", "microservice"))
            s.merge(ParcoursTrack.BACKEND_JAVA, 6, Integer::sum);
        if (containsAny(ctx, "backend developer", "developpeur backend", "ingenieur java"))
            s.merge(ParcoursTrack.BACKEND_JAVA, 7, Integer::sum);
    }

    private void scoreBackendNode(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "node.js", "nodejs", "express", "nestjs", "nest.js", "typescript backend"))
            s.merge(ParcoursTrack.BACKEND_NODE, 9, Integer::sum);
    }

    private void scoreBackendPython(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "python", "django", "flask", "fastapi"))
            s.merge(ParcoursTrack.BACKEND_PYTHON, 9, Integer::sum);
    }

    private void scoreFrontend(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "frontend", "front-end", "react", "angular", "vue", "nextjs", "next.js", "typescript", "ui developer"))
            s.merge(ParcoursTrack.FRONTEND_WEB, 8, Integer::sum);
    }

    private void scoreMobile(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "mobile", "android", "ios", "swift", "kotlin", "flutter", "react native"))
            s.merge(ParcoursTrack.MOBILE, 9, Integer::sum);
    }

    private void scoreDataScience(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "data science", "data scientist", "machine learning", "ml ", " deep learning",
                "pandas", "tensorflow", "pytorch", "analyste donnees", "analyste données"))
            s.merge(ParcoursTrack.DATA_SCIENCE, 9, Integer::sum);
    }

    private void scoreQa(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "qa ", " qa", "quality assurance", "testeur", "test engineer", "selenium", "cypress", "jest"))
            s.merge(ParcoursTrack.QA, 8, Integer::sum);
    }

    private void scoreDevOps(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "devops", "sre", "kubernetes", "k8s", "terraform", "ansible", "ci/cd", "jenkins", "platform engineer"))
            s.merge(ParcoursTrack.DEVOPS, 8, Integer::sum);
    }

    private void scoreCybersec(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "cyber", "securite", "sécurité", "soc ", "pentest", "iso 27001", "security engineer"))
            s.merge(ParcoursTrack.CYBERSEC, 9, Integer::sum);
    }

    private void scoreGenericTech(String ctx, Map<ParcoursTrack, Integer> s) {
        if (containsAny(ctx, "tech", "software", "developer", "developpeur", "ingenieur", "it ", "dsi", "digital", "engineering"))
            s.merge(ParcoursTrack.GENERIC_TECH, 3, Integer::sum);
    }

    private int addHrTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Formation politiques RH", "Droit du travail, diversité, performance, procédures internes.",
                TypeEtape.HUMAIN, o++, 4, true, "https://www.youtube.com/watch?v=7dKjPk-Gc7Y", parcours));
        out.add(step("Affiliation avantages sociaux", "Mutuelle, retraite, congés — documents et délais.",
                TypeEtape.ADMINISTRATIF, o++, 3, true, null, parcours));
        out.add(step("Présentation équipe RH & SIRH", "Outils RH, interlocuteurs et calendrier d'activités.",
                TypeEtape.HUMAIN, o++, 5, false, null, parcours));
        return o;
    }

    private int addBackendJavaTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Environnement Java (JDK 17+, Maven/Gradle, IDE)",
                "IntelliJ ou VS Code, Git, Docker, conventions de formatage et structure des modules.",
                TypeEtape.TECHNIQUE, o++, 4, true,
                "https://spring.io/guides/gs/spring-boot/,https://www.youtube.com/watch?v=9SGDpanA8no", parcours));
        out.add(step("Formation Spring Boot — fondamentaux",
                "Bootstrapping, application.yml, profils, Actuator, couches Controller/Service/Repository, injection de dépendances.",
                TypeEtape.TECHNIQUE, o++, 8, false,
                "https://spring.io/guides/gs/rest-service/,https://www.baeldung.com/spring-boot", parcours));
        out.add(step("API REST & Spring Data JPA",
                "DTO, Bean Validation, codes HTTP, entités JPA, transactions, migrations Flyway/Liquibase.",
                TypeEtape.TECHNIQUE, o++, 7, false,
                "https://spring.io/guides/gs/accessing-data-jpa/", parcours));
        out.add(step("Spring Security & tests (JUnit 5, MockMvc)",
                "Auth JWT/session selon stack interne, tests unitaires et d'intégration, SonarQube.",
                TypeEtape.TECHNIQUE, o++, 6, true,
                "https://spring.io/guides/topicals/spring-security-architecture", parcours));
        out.add(step("Microservices & messaging (optionnel)",
                "Découverte des services internes, OpenAPI/Swagger, Kafka/RabbitMQ si utilisé.",
                TypeEtape.TECHNIQUE, o++, 5, false, null, parcours));
        out.add(step("Git, CI/CD & revue de code backend",
                "Gitflow, pipelines Maven, merge requests et standards de l'équipe.",
                TypeEtape.TECHNIQUE, o++, 3, false, null, parcours));
        out.add(step("Pairing métier & premier ticket",
                "Ticket guidé avec un référent : lecture du domaine, dette technique, Definition of Done.",
                TypeEtape.HUMAIN, o++, 7, false, null, parcours));
        return o;
    }

    private int addBackendNodeTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Environnement Node.js & TypeScript",
                "Node LTS, npm/pnpm, ESLint, structure projet Express/NestJS.",
                TypeEtape.TECHNIQUE, o++, 4, true, "https://nodejs.org/en/docs/guides", parcours));
        out.add(step("API REST avec Express ou NestJS",
                "Routing, middleware, validation, ORM (Prisma/TypeORM), gestion d'erreurs.",
                TypeEtape.TECHNIQUE, o++, 7, false, "https://docs.nestjs.com/", parcours));
        out.add(step("Auth, tests & déploiement Node",
                "JWT, tests Jest/Supertest, variables d'environnement, Docker.",
                TypeEtape.TECHNIQUE, o++, 6, true, null, parcours));
        return o;
    }

    private int addBackendPythonTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Environnement Python & virtualenv",
                "Python 3.11+, venv/poetry, IDE, linting (ruff/black).",
                TypeEtape.TECHNIQUE, o++, 3, true, null, parcours));
        out.add(step("API avec FastAPI ou Django REST",
                "Modèles, serializers, endpoints, auth, tests pytest.",
                TypeEtape.TECHNIQUE, o++, 8, false, "https://fastapi.tiangolo.com/tutorial/", parcours));
        out.add(step("Bases de données & tâches async",
                "SQLAlchemy/ORM, Celery ou équivalent, bonnes pratiques perf.",
                TypeEtape.TECHNIQUE, o++, 6, false, null, parcours));
        return o;
    }

    private int addFrontendTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Environnement front (Node, bundler, IDE)",
                "CRA/Vite, ESLint/Prettier, debug navigateur.",
                TypeEtape.TECHNIQUE, o++, 4, true, "https://react.dev/learn", parcours));
        out.add(step("React / framework & state management",
                "Composants, hooks, routing, appels API, formulaires accessibles.",
                TypeEtape.TECHNIQUE, o++, 8, false, "https://react.dev/reference/react", parcours));
        out.add(step("Tests UI & design system",
                "Jest/Vitest, Testing Library, Figma et tokens du design system.",
                TypeEtape.TECHNIQUE, o++, 5, false, null, parcours));
        return o;
    }

    private int addFullstackTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        o = addBackendJavaTrack(out, parcours, o);
        out.add(step("Intégration front ↔ API Spring",
                "Consommation REST depuis React, CORS, gestion des tokens, environnements dev.",
                TypeEtape.TECHNIQUE, o++, 5, false, "https://spring.io/guides/tutorials/react/", parcours));
        o = addFrontendTrack(out, parcours, o);
        return o;
    }

    private int addMobileTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Environnement mobile (Android Studio / Xcode)",
                "SDK, émulateurs, certificats de dev, conventions de build.",
                TypeEtape.TECHNIQUE, o++, 5, true, null, parcours));
        out.add(step("Architecture app & cycle de release",
                "Navigation, state, stores, pipeline App Store / Play Store.",
                TypeEtape.TECHNIQUE, o++, 7, false, null, parcours));
        return o;
    }

    private int addDataScienceTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Accès données & notebooks",
                "Warehouse/lake interne, Jupyter, politique de données sensibles.",
                TypeEtape.TECHNIQUE, o++, 4, true, null, parcours));
        out.add(step("Pipeline ML & évaluation modèles",
                "Feature engineering, entraînement, métriques, MLOps de base.",
                TypeEtape.TECHNIQUE, o++, 8, false, "https://scikit-learn.org/stable/tutorial/index.html", parcours));
        out.add(step("Visualisation & restitution métier",
                "Dashboards, storytelling data, documentation des modèles.",
                TypeEtape.HUMAIN, o++, 5, false, null, parcours));
        return o;
    }

    private int addQaTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Stratégie de test & pyramide QA",
                "Unit, intégration, E2E, critères d'acceptation, outils internes.",
                TypeEtape.TECHNIQUE, o++, 4, true, null, parcours));
        out.add(step("Automatisation (Selenium/Cypress/Postman)",
                "Scénarios critiques, CI, reporting des bugs.",
                TypeEtape.TECHNIQUE, o++, 7, false, null, parcours));
        return o;
    }

    private int addDevOpsTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Accès infra, cloud & secrets",
                "Comptes cloud, Vault, VPN, least-privilege.",
                TypeEtape.TECHNIQUE, o++, 4, true, null, parcours));
        out.add(step("CI/CD & Infrastructure as Code",
                "Pipelines, Terraform/Helm, promotion d'artifacts.",
                TypeEtape.TECHNIQUE, o++, 8, false, "https://kubernetes.io/docs/tutorials/", parcours));
        out.add(step("Observabilité & gestion d'incidents",
                "Logs, métriques, alertes, post-mortems.",
                TypeEtape.TECHNIQUE, o++, 5, false, null, parcours));
        return o;
    }

    private int addCybersecTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Sensibilisation sécurité & conformité",
                "RGPD, gestion des accès, phishing, charte sécurité.",
                TypeEtape.ADMINISTRATIF, o++, 3, true, null, parcours));
        out.add(step("Outils SOC & bonnes pratiques défensives",
                "SIEM, durcissement, revue des vulnérabilités.",
                TypeEtape.TECHNIQUE, o++, 7, false, null, parcours));
        return o;
    }

    private int addGenericTechTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Setup environnement de développement",
                "IDE, Git, build local, accès staging.",
                TypeEtape.TECHNIQUE, o++, 5, true, null, parcours));
        out.add(step("Dépôt & standards de code",
                "Branches, PR, revue de code, documentation technique.",
                TypeEtape.TECHNIQUE, o++, 2, false, null, parcours));
        out.add(step("Formation stack produit interne",
                "Architecture, modules clés, mentorat technique.",
                TypeEtape.HUMAIN, o++, 7, false, null, parcours));
        return o;
    }

    private int addNonTechTrack(List<Etape> out, OnboardingParcours parcours, int o) {
        out.add(step("Formation métier du département",
                "Rôle, processus clés, interactions inter-services.",
                TypeEtape.HUMAIN, o++, 5, true, null, parcours));
        out.add(step("Outils & reporting du service",
                "SOP, indicateurs, canaux de communication.",
                TypeEtape.ADMINISTRATIF, o++, 3, false, null, parcours));
        out.add(step("Mentorat & intégration culturelle",
                "Binôme référent, objectifs court terme.",
                TypeEtape.HUMAIN, o++, 7, false, null, parcours));
        return o;
    }

    /**
     * Legacy step builder that takes an OnboardingParcours entity directly.
     */
    private Etape step(String nom, String description, TypeEtape type, int ordre, int duree,
                       boolean doc, String resourceLinks, OnboardingParcours parcours) {
        Etape etape = new Etape();
        etape.setNom(nom);
        etape.setDescription(description);
        etape.setType(type);
        etape.setOrdreExecution(ordre);
        etape.setDureeEstimee(duree);
        etape.setRequiertDocument(doc);
        if (resourceLinks != null && !resourceLinks.isBlank()) {
            etape.setResourceLinks(resourceLinks);
        }
        etape.setParcours(parcours);
        return etape;
    }

    private static String join(String... parts) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) b.append(p).append(' ');
        }
        return b.toString();
    }

    private static String normalizeForMatch(String input) {
        if (input == null || input.isBlank()) return "";
        return Normalizer.normalize(input.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }

    private static boolean containsAny(String normalizedCtx, String... needles) {
        for (String n : needles) {
            if (n == null || n.isBlank()) continue;
            if (normalizedCtx.contains(normalizeForMatch(n))) return true;
        }
        return false;
    }
}
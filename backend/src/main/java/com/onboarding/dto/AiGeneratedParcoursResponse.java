package com.onboarding.dto;

import com.onboarding.entity.Etape;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutParcours;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Réponse de POST /api/parcours/ai-generate : parcours créé + étapes générées.
 * (L'entité OnboardingParcours masque {@code etapes} en JSON via @JsonIgnore.)
 */
public class AiGeneratedParcoursResponse {

    private Long id;
    private String nom;
    private String description;
    private String categorieCible;
    private String departementCible;
    private Integer dureeGlobaleEstimee;
    private Integer deadlineGlobaleParDefaut;
    private StatutParcours statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private List<Etape> etapes;
    private int etapeCount;

    public static AiGeneratedParcoursResponse from(OnboardingParcours parcours, List<Etape> etapes) {
        AiGeneratedParcoursResponse r = new AiGeneratedParcoursResponse();
        r.id = parcours.getId();
        r.nom = parcours.getNom();
        r.description = parcours.getDescription();
        r.categorieCible = parcours.getCategorieCible();
        r.departementCible = parcours.getDepartementCible();
        r.dureeGlobaleEstimee = parcours.getDureeGlobaleEstimee();
        r.deadlineGlobaleParDefaut = parcours.getDeadlineGlobaleParDefaut();
        r.statut = parcours.getStatut();
        r.dateCreation = parcours.getDateCreation();
        r.dateModification = parcours.getDateModification();
        r.etapes = etapes;
        r.etapeCount = etapes != null ? etapes.size() : 0;
        return r;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getTitre() { return nom; }
    public String getDescription() { return description; }
    public String getCategorieCible() { return categorieCible; }
    public String getDepartementCible() { return departementCible; }
    public Integer getDureeGlobaleEstimee() { return dureeGlobaleEstimee; }
    public Integer getDeadlineGlobaleParDefaut() { return deadlineGlobaleParDefaut; }
    public StatutParcours getStatut() { return statut; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public List<Etape> getEtapes() { return etapes; }
    public int getEtapeCount() { return etapeCount; }
}

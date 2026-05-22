package com.onboarding.dto;

import com.onboarding.entity.StatutParcours;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class OnboardingParcoursDTO {
    
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    private String description;
    
    @NotBlank(message = "La catégorie cible est obligatoire")
    private String categorieCible;
    
    @NotBlank(message = "Le département cible est obligatoire")
    private String departementCible;
    
    private Integer dureeGlobaleEstimee;
    
    private Integer deadlineGlobaleParDefaut;
    
    private StatutParcours statut;
    
    private LocalDateTime dateCreation;
    
    private LocalDateTime dateModification;
    
    // Constructors
    public OnboardingParcoursDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategorieCible() { return categorieCible; }
    public void setCategorieCible(String categorieCible) { this.categorieCible = categorieCible; }
    
    public String getDepartementCible() { return departementCible; }
    public void setDepartementCible(String departementCible) { this.departementCible = departementCible; }
    
    public Integer getDureeGlobaleEstimee() { return dureeGlobaleEstimee; }
    public void setDureeGlobaleEstimee(Integer dureeGlobaleEstimee) { this.dureeGlobaleEstimee = dureeGlobaleEstimee; }
    
    public Integer getDeadlineGlobaleParDefaut() { return deadlineGlobaleParDefaut; }
    public void setDeadlineGlobaleParDefaut(Integer deadlineGlobaleParDefaut) { this.deadlineGlobaleParDefaut = deadlineGlobaleParDefaut; }
    
    public StatutParcours getStatut() { return statut; }
    public void setStatut(StatutParcours statut) { this.statut = statut; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}

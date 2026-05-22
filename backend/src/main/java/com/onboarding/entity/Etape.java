package com.onboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "etapes")
public class Etape {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom de l'étape est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "resource_links", columnDefinition = "TEXT")
    private String resourceLinks;

    @NotNull(message = "Le type est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeEtape type;
    
    @Column(name = "duree_estimee")
    private Integer dureeEstimee;
    
    @Column(name = "ordre_execution")
    private Integer ordreExecution;
    
    @Column(name = "requiert_document", nullable = false)
    private Boolean requiertDocument = false;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id", nullable = false)
    @JsonIgnore
    private OnboardingParcours parcours;
    
    // Constructors
    public Etape() {}
    
    public Etape(String nom, TypeEtape type, OnboardingParcours parcours) {
        this.nom = nom;
        this.type = type;
        this.parcours = parcours;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { 
        this.nom = nom;
        this.dateModification = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.dateModification = LocalDateTime.now();
    }

    public String getResourceLinks() { return resourceLinks; }
    public void setResourceLinks(String resourceLinks) { 
        this.resourceLinks = resourceLinks;
        this.dateModification = LocalDateTime.now();
    }
    
    public TypeEtape getType() { return type; }
    public void setType(TypeEtape type) { 
        this.type = type;
        this.dateModification = LocalDateTime.now();
    }
    
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { 
        this.dureeEstimee = dureeEstimee;
        this.dateModification = LocalDateTime.now();
    }
    
    public Integer getOrdreExecution() { return ordreExecution; }
    public void setOrdreExecution(Integer ordreExecution) { 
        this.ordreExecution = ordreExecution;
        this.dateModification = LocalDateTime.now();
    }
    
    public Boolean getRequiertDocument() { return requiertDocument; }
    public void setRequiertDocument(Boolean requiertDocument) { 
        this.requiertDocument = requiertDocument;
        this.dateModification = LocalDateTime.now();
    }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    public OnboardingParcours getParcours() { return parcours; }
    public void setParcours(OnboardingParcours parcours) { this.parcours = parcours; }

    /** Exposé en JSON uniquement — pas un champ JPA (évite de casser findByParcours_Id). */
    @Transient
    @JsonProperty("parcoursId")
    public Long getParcoursIdForJson() {
        return parcours != null ? parcours.getId() : null;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}

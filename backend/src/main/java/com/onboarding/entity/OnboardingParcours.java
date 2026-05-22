package com.onboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "onboarding_parcours")
public class OnboardingParcours {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotBlank(message = "La catégorie cible est obligatoire")
    @Column(name = "categorie_cible", nullable = false)
    private String categorieCible;
    
    @NotBlank(message = "Le département cible est obligatoire")
    @Column(name = "departement_cible", nullable = false)
    private String departementCible;
    
    @Column(name = "duree_globale_estimee")
    private Integer dureeGlobaleEstimee;
    
    @Column(name = "deadline_globale_par_defaut")
    private Integer deadlineGlobaleParDefaut;
    
    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutParcours statut = StatutParcours.ACTIF;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @OneToMany(mappedBy = "parcours", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Etape> etapes;
    
    // Constructors
    public OnboardingParcours() {}
    
    public OnboardingParcours(String nom, String categorieCible, String departementCible) {
        this.nom = nom;
        this.categorieCible = categorieCible;
        this.departementCible = departementCible;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { 
        this.nom = nom;
        this.dateModification = LocalDateTime.now();
    }
    
    public String getTitre() { return nom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.dateModification = LocalDateTime.now();
    }
    
    public String getCategorieCible() { return categorieCible; }
    public void setCategorieCible(String categorieCible) { 
        this.categorieCible = categorieCible;
        this.dateModification = LocalDateTime.now();
    }
    
    public String getDepartementCible() { return departementCible; }
    public void setDepartementCible(String departementCible) { 
        this.departementCible = departementCible;
        this.dateModification = LocalDateTime.now();
    }
    
    public Integer getDureeGlobaleEstimee() { return dureeGlobaleEstimee; }
    public void setDureeGlobaleEstimee(Integer dureeGlobaleEstimee) { 
        this.dureeGlobaleEstimee = dureeGlobaleEstimee;
        this.dateModification = LocalDateTime.now();
    }
    
    public Integer getDeadlineGlobaleParDefaut() { return deadlineGlobaleParDefaut; }
    public void setDeadlineGlobaleParDefaut(Integer deadlineGlobaleParDefaut) { 
        this.deadlineGlobaleParDefaut = deadlineGlobaleParDefaut;
        this.dateModification = LocalDateTime.now();
    }
    
    public StatutParcours getStatut() { return statut; }
    public void setStatut(StatutParcours statut) { 
        this.statut = statut;
        this.dateModification = LocalDateTime.now();
    }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    public List<Etape> getEtapes() { return etapes; }
    public void setEtapes(List<Etape> etapes) { this.etapes = etapes; }
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}

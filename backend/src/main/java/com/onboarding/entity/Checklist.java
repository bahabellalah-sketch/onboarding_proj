package com.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "checklists")
public class Checklist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etape_id")
    private Etape etape;
    
    @Column(name = "titre", nullable = false)
    private String titre;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutChecklist statut;
    
    @Column(name = "ordre")
    private Integer ordre;
    
    @Column(name = "obligatoire", nullable = false)
    private Boolean obligatoire = true;
    
    @Column(name = "requiert_document", nullable = false)
    private Boolean requiertDocument = false;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @Column(name = "date_realisation")
    private LocalDate dateRealisation;
    
    @Column(name = "cree_par")
    private String creePar;
    
    public Checklist() {
        this.dateCreation = LocalDate.now();
        this.statut = StatutChecklist.EN_ATTENTE;
        this.ordre = 1;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Assignment getAssignment() {
        return assignment;
    }
    
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    
    public Etape getEtape() {
        return etape;
    }
    
    public void setEtape(Etape etape) {
        this.etape = etape;
    }
    
    public String getTitre() {
        return titre;
    }
    
    public void setTitre(String titre) {
        this.titre = titre;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public StatutChecklist getStatut() {
        return statut;
    }
    
    public void setStatut(StatutChecklist statut) {
        this.statut = statut;
    }
    
    public Integer getOrdre() {
        return ordre;
    }
    
    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
    
    public Boolean getObligatoire() {
        return obligatoire;
    }
    
    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }
    
    public Boolean getRequiertDocument() {
        return requiertDocument;
    }
    
    public void setRequiertDocument(Boolean requiertDocument) {
        this.requiertDocument = requiertDocument;
    }
    
    public LocalDate getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateRealisation() {
        return dateRealisation;
    }
    
    public void setDateRealisation(LocalDate dateRealisation) {
        this.dateRealisation = dateRealisation;
    }
    
    public String getCreePar() {
        return creePar;
    }
    
    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }
}

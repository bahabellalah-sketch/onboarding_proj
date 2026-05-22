package com.onboarding.dto;

import java.time.LocalDate;

public class ChecklistDTO {
    private Long id;
    private Long assignmentId;
    private String titre;
    private String description;
    private String statut;
    private Integer ordre;
    private Boolean obligatoire;
    private Boolean requiertDocument;
    private LocalDate dateCreation;
    private LocalDate dateRealisation;
    private String creePar;
    private Boolean unlocked;
    private Boolean lockedCompleted;
    private Long etapeId;

    // Constructeurs
    public ChecklistDTO() {}

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
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

    public Boolean getUnlocked() {
        return unlocked;
    }

    public void setUnlocked(Boolean unlocked) {
        this.unlocked = unlocked;
    }

    public Boolean getLockedCompleted() {
        return lockedCompleted;
    }

    public void setLockedCompleted(Boolean lockedCompleted) {
        this.lockedCompleted = lockedCompleted;
    }

    public Long getEtapeId() {
        return etapeId;
    }

    public void setEtapeId(Long etapeId) {
        this.etapeId = etapeId;
    }
}

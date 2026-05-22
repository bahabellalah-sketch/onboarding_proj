package com.onboarding.dto;

import java.time.LocalDate;

public class AssignmentDTO {
    
    private Long id;
    private Long userId;
    private Long parcoursId;
    private String userName;
    private String userPrenom;
    private String userEmail;
    private String parcoursNom;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevisionnelle;
    private LocalDate dateFinReelle;
    private String statut;
    private Integer pourcentageAvancement;
    private LocalDate dateCreation;
    private LocalDate dateModification;
    private String assignePar;
    
    // Constructeurs
    public AssignmentDTO() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getParcoursId() {
        return parcoursId;
    }
    
    public void setParcoursId(Long parcoursId) {
        this.parcoursId = parcoursId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserPrenom() {
        return userPrenom;
    }
    
    public void setUserPrenom(String userPrenom) {
        this.userPrenom = userPrenom;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getParcoursNom() {
        return parcoursNom;
    }
    
    public void setParcoursNom(String parcoursNom) {
        this.parcoursNom = parcoursNom;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }
    
    public LocalDate getDateFinPrevisionnelle() {
        return dateFinPrevisionnelle;
    }
    
    public void setDateFinPrevisionnelle(LocalDate dateFinPrevisionnelle) {
        this.dateFinPrevisionnelle = dateFinPrevisionnelle;
    }
    
    public LocalDate getDateFinReelle() {
        return dateFinReelle;
    }
    
    public void setDateFinReelle(LocalDate dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public Integer getPourcentageAvancement() {
        return pourcentageAvancement;
    }
    
    public void setPourcentageAvancement(Integer pourcentageAvancement) {
        this.pourcentageAvancement = pourcentageAvancement;
    }
    
    public LocalDate getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDate dateModification) {
        this.dateModification = dateModification;
    }
    
    public String getAssignePar() {
        return assignePar;
    }
    
    public void setAssignePar(String assignePar) {
        this.assignePar = assignePar;
    }
}

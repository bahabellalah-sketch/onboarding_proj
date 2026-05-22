package com.onboarding.dto;

import com.onboarding.entity.TypeEtape;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class EtapeDTO {
    
    private Long id;
    
    @NotBlank(message = "Le nom de l'étape est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le type est obligatoire")
    private TypeEtape type;
    
    private Integer dureeEstimee;
    
    private Integer ordreExecution;
    
    private Boolean requiertDocument;
    
    private Long parcoursId;
    
    private LocalDateTime dateCreation;
    
    private LocalDateTime dateModification;
    
    // Constructors
    public EtapeDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public TypeEtape getType() { return type; }
    public void setType(TypeEtape type) { this.type = type; }
    
    public Integer getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(Integer dureeEstimee) { this.dureeEstimee = dureeEstimee; }
    
    public Integer getOrdreExecution() { return ordreExecution; }
    public void setOrdreExecution(Integer ordreExecution) { this.ordreExecution = ordreExecution; }
    
    public Boolean getRequiertDocument() { return requiertDocument; }
    public void setRequiertDocument(Boolean requiertDocument) { this.requiertDocument = requiertDocument; }
    
    public Long getParcoursId() { return parcoursId; }
    public void setParcoursId(Long parcoursId) { this.parcoursId = parcoursId; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}

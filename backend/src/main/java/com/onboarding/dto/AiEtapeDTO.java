package com.onboarding.dto;

/**
 * DTO for parsing AI-generated task JSON from the LLM response.
 */
public class AiEtapeDTO {

    private String nom;
    private String description;
    private String type;
    private int dureeEstimee;
    private boolean requiertDocument;
    private String resourceLinks;

    public AiEtapeDTO() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDureeEstimee() { return dureeEstimee; }
    public void setDureeEstimee(int dureeEstimee) { this.dureeEstimee = dureeEstimee; }

    public boolean isRequiertDocument() { return requiertDocument; }
    public void setRequiertDocument(boolean requiertDocument) { this.requiertDocument = requiertDocument; }

    public String getResourceLinks() { return resourceLinks; }
    public void setResourceLinks(String resourceLinks) { this.resourceLinks = resourceLinks; }
}
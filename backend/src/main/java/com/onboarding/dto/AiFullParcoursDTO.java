package com.onboarding.dto;

import java.util.List;

/**
 * DTO that represents the full AI-generated output: parcours + tasks.
 * This matches the JSON structure the AI is instructed to return.
 */
public class AiFullParcoursDTO {

    private String nom;
    private String description;
    private String categorieCible;
    private String departementCible;
    private Integer dureeGlobaleEstimee;
    private List<AiEtapeDTO> etapes;

    public AiFullParcoursDTO() {}

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

    public List<AiEtapeDTO> getEtapes() { return etapes; }
    public void setEtapes(List<AiEtapeDTO> etapes) { this.etapes = etapes; }
}
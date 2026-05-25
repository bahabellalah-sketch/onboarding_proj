package com.onboarding.dto;

import java.time.LocalDate;

public class PendingManagerEvaluationDTO {
    private Long assignmentId;
    private Long collaborateurId;
    private String collaborateurNom;
    private String collaborateurEmail;
    private String parcoursNom;
    private Integer pourcentageAvancement;
    private String statut;
    private LocalDate dateFinReelle;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public Long getCollaborateurId() { return collaborateurId; }
    public void setCollaborateurId(Long collaborateurId) { this.collaborateurId = collaborateurId; }

    public String getCollaborateurNom() { return collaborateurNom; }
    public void setCollaborateurNom(String collaborateurNom) { this.collaborateurNom = collaborateurNom; }

    public String getCollaborateurEmail() { return collaborateurEmail; }
    public void setCollaborateurEmail(String collaborateurEmail) { this.collaborateurEmail = collaborateurEmail; }

    public String getParcoursNom() { return parcoursNom; }
    public void setParcoursNom(String parcoursNom) { this.parcoursNom = parcoursNom; }

    public Integer getPourcentageAvancement() { return pourcentageAvancement; }
    public void setPourcentageAvancement(Integer pourcentageAvancement) { this.pourcentageAvancement = pourcentageAvancement; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateFinReelle() { return dateFinReelle; }
    public void setDateFinReelle(LocalDate dateFinReelle) { this.dateFinReelle = dateFinReelle; }
}

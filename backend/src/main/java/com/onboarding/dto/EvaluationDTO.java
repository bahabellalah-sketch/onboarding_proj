package com.onboarding.dto;

import com.onboarding.entity.EvaluationType;
import java.time.LocalDateTime;

public class EvaluationDTO {
    private Long id;
    private EvaluationType evaluationType;
    private Long checklistId;
    private String checklistTitre;
    private Long assignmentId;
    private String parcoursNom;
    private Long collaborateurId;
    private String collaborateurNom;
    private String collaborateurEmail;
    private Long evaluatorId;
    private String evaluatorNom;
    private String evaluatorEmail;
    private String evaluatorRole;
    private Integer rating;
    private String comment;
    private String recommendation;
    private LocalDateTime dateEvaluation;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    public Long getChecklistId() { return checklistId; }
    public void setChecklistId(Long checklistId) { this.checklistId = checklistId; }

    public String getChecklistTitre() { return checklistTitre; }
    public void setChecklistTitre(String checklistTitre) { this.checklistTitre = checklistTitre; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getParcoursNom() { return parcoursNom; }
    public void setParcoursNom(String parcoursNom) { this.parcoursNom = parcoursNom; }

    public Long getCollaborateurId() { return collaborateurId; }
    public void setCollaborateurId(Long collaborateurId) { this.collaborateurId = collaborateurId; }

    public String getCollaborateurNom() { return collaborateurNom; }
    public void setCollaborateurNom(String collaborateurNom) { this.collaborateurNom = collaborateurNom; }

    public String getCollaborateurEmail() { return collaborateurEmail; }
    public void setCollaborateurEmail(String collaborateurEmail) { this.collaborateurEmail = collaborateurEmail; }

    public Long getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }

    public String getEvaluatorNom() { return evaluatorNom; }
    public void setEvaluatorNom(String evaluatorNom) { this.evaluatorNom = evaluatorNom; }

    public String getEvaluatorEmail() { return evaluatorEmail; }
    public void setEvaluatorEmail(String evaluatorEmail) { this.evaluatorEmail = evaluatorEmail; }

    public String getEvaluatorRole() { return evaluatorRole; }
    public void setEvaluatorRole(String evaluatorRole) { this.evaluatorRole = evaluatorRole; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDateTime dateEvaluation) { this.dateEvaluation = dateEvaluation; }
}

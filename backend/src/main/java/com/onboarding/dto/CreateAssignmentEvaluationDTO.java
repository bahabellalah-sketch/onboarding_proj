package com.onboarding.dto;

import com.onboarding.entity.EvaluationType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateAssignmentEvaluationDTO {

    @NotNull
    private Long assignmentId;

    @NotNull
    private EvaluationType evaluationType;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    /** OUI | NON | AVEC_ACCOMPAGNEMENT — pour PARCOURS_MANAGER */
    private String recommendation;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}

package com.onboarding.dto;

public class AssignmentEvaluationSummaryDTO {
    private Long assignmentId;
    private boolean canEvaluateCollab;
    private boolean canEvaluateManager;
    private EvaluationDTO collabEvaluation;
    private EvaluationDTO managerEvaluation;
    private double averageStepRating;
    private long stepEvaluationCount;

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public boolean isCanEvaluateCollab() { return canEvaluateCollab; }
    public void setCanEvaluateCollab(boolean canEvaluateCollab) { this.canEvaluateCollab = canEvaluateCollab; }

    public boolean isCanEvaluateManager() { return canEvaluateManager; }
    public void setCanEvaluateManager(boolean canEvaluateManager) { this.canEvaluateManager = canEvaluateManager; }

    public EvaluationDTO getCollabEvaluation() { return collabEvaluation; }
    public void setCollabEvaluation(EvaluationDTO collabEvaluation) { this.collabEvaluation = collabEvaluation; }

    public EvaluationDTO getManagerEvaluation() { return managerEvaluation; }
    public void setManagerEvaluation(EvaluationDTO managerEvaluation) { this.managerEvaluation = managerEvaluation; }

    public double getAverageStepRating() { return averageStepRating; }
    public void setAverageStepRating(double averageStepRating) { this.averageStepRating = averageStepRating; }

    public long getStepEvaluationCount() { return stepEvaluationCount; }
    public void setStepEvaluationCount(long stepEvaluationCount) { this.stepEvaluationCount = stepEvaluationCount; }
}

package com.onboarding.dto;

import com.onboarding.entity.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UserReportDTO {
    
    @NotNull(message = "L'ID de l'utilisateur signalé est obligatoire")
    private Long reportedUserId;
    
    @NotNull(message = "L'ID du type de signalement est obligatoire")
    private ReportType reportType;
    
    @NotBlank(message = "La raison du signalement est obligatoire")
    private String reason;
    
    // Constructors
    public UserReportDTO() {}
    
    public UserReportDTO(Long reportedUserId, ReportType reportType, String reason) {
        this.reportedUserId = reportedUserId;
        this.reportType = reportType;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }
    
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

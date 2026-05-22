package com.onboarding.dto;

import java.time.LocalDateTime;

public class UserReportResponseDTO {
    private Long id;
    private Long reportedUserId;
    private String reportedUserName;
    private String reportedUserEmail;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private String reportType;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long resolvedById;
    private String resolvedByName;
    private String adminNotes;

    // Constructors
    public UserReportResponseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }

    public String getReportedUserName() { return reportedUserName; }
    public void setReportedUserName(String reportedUserName) { this.reportedUserName = reportedUserName; }

    public String getReportedUserEmail() { return reportedUserEmail; }
    public void setReportedUserEmail(String reportedUserEmail) { this.reportedUserEmail = reportedUserEmail; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public Long getResolvedById() { return resolvedById; }
    public void setResolvedById(Long resolvedById) { this.resolvedById = resolvedById; }

    public String getResolvedByName() { return resolvedByName; }
    public void setResolvedByName(String resolvedByName) { this.resolvedByName = resolvedByName; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
}

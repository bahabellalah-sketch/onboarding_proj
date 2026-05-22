package com.onboarding.service;

import com.onboarding.dto.UserReportResponseDTO;
import com.onboarding.entity.ReportStatus;
import com.onboarding.entity.User;
import com.onboarding.entity.UserReport;
import com.onboarding.entity.ReportType;
import com.onboarding.repository.UserReportRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserReportService {
    
    @Autowired
    private UserReportRepository userReportRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public UserReport createReport(
            @NonNull Long reportedUserId,
            @NonNull Long reporterId,
            @NonNull ReportType reportType,
            @NonNull String reason
    ) {
        // Vérifier que les utilisateurs existent
        User reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur signalé non trouvé"));
        
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur qui signale non trouvé"));
        
        // Vérifier que l'utilisateur ne se signale pas lui-même
        if (reportedUser.getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("Un utilisateur ne peut pas se signaler lui-même");
        }
        
        // Vérifier qu'un signalement similaire n'existe pas déjà
        if (userReportRepository.existsByReportedUserAndReporterAndStatus(
                reportedUser, reporter, ReportStatus.PENDING)) {
            throw new IllegalArgumentException("Un signalement pour cet utilisateur est déjà en cours de traitement");
        }
        
        // Créer le signalement
        UserReport report = new UserReport(reportedUser, reporter, reportType, reason);
        
        return userReportRepository.save(report);
    }
    
    public List<UserReport> getPendingReports() {
        return userReportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);
    }
    
    public List<UserReport> getReportsByUserId(@NonNull Long userId) {
        return userReportRepository.findReportsByUserId(userId);
    }
    
    public UserReport resolveReport(
            @NonNull Long reportId,
            @NonNull Long adminId,
            @NonNull ReportStatus finalStatus,
            String adminNotes
    ) {
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Signalement non trouvé"));
        
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Administrateur non trouvé"));
        
        report.setStatus(finalStatus);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(admin);
        report.setAdminNotes(adminNotes);
        
        UserReport savedReport = userReportRepository.save(report);
        
        // Send email notification if report is approved (RESOLVED)
        if (finalStatus == ReportStatus.RESOLVED) {
            System.out.println("UserReportService: Sending email for RESOLVED report");
            String reportedUserName = report.getReportedUser().getPrenom() + " " + report.getReportedUser().getNom();
            String reporterName = report.getReporter().getPrenom() + " " + report.getReporter().getNom();
            String reportType = report.getReportType().toString();
            
            System.out.println("UserReportService: Email to: " + report.getReportedUser().getEmail());
            System.out.println("UserReportService: User: " + reportedUserName);
            System.out.println("UserReportService: Report type: " + reportType);
            System.out.println("UserReportService: Reporter: " + reporterName);
            System.out.println("UserReportService: Admin notes: " + adminNotes);
            
            emailService.sendReportApprovedEmail(
                report.getReportedUser().getEmail(),
                reportedUserName,
                adminNotes,
                reportType,
                reporterName
            );
            
            System.out.println("UserReportService: Email sent successfully");
        } else {
            System.out.println("UserReportService: Report status is " + finalStatus + ", not sending email");
        }
        
        return savedReport;
    }
    
    public List<UserReport> getAllReports() {
        return userReportRepository.findAll();
    }
    
    public List<UserReportResponseDTO> getAllReportsDTO() {
        List<UserReport> reports = userReportRepository.findAll();
        return reports.stream().map(report -> {
            UserReportResponseDTO dto = new UserReportResponseDTO();
            dto.setId(report.getId());
            dto.setReportedUserId(report.getReportedUser().getId());
            dto.setReportedUserName(report.getReportedUser().getPrenom() + " " + report.getReportedUser().getNom());
            dto.setReportedUserEmail(report.getReportedUser().getEmail());
            dto.setReporterId(report.getReporter().getId());
            dto.setReporterName(report.getReporter().getPrenom() + " " + report.getReporter().getNom());
            dto.setReporterEmail(report.getReporter().getEmail());
            dto.setReportType(report.getReportType().toString());
            dto.setReason(report.getReason());
            dto.setStatus(report.getStatus().toString());
            dto.setCreatedAt(report.getCreatedAt());
            dto.setResolvedAt(report.getResolvedAt());
            if (report.getResolvedBy() != null) {
                dto.setResolvedById(report.getResolvedBy().getId());
                dto.setResolvedByName(report.getResolvedBy().getPrenom() + " " + report.getResolvedBy().getNom());
            }
            dto.setAdminNotes(report.getAdminNotes());
            return dto;
        }).collect(Collectors.toList());
    }
}

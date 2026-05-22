package com.onboarding.controller;

import com.onboarding.dto.UserReportDTO;
import com.onboarding.dto.UserReportResponseDTO;
import com.onboarding.entity.ReportStatus;
import com.onboarding.entity.ReportType;
import com.onboarding.entity.User;
import com.onboarding.entity.UserReport;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.UserReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
@SuppressWarnings("nullness")
public class UserReportController {
    
    @Autowired
    private UserReportService userReportService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<?> createReport(@Valid @RequestBody UserReportDTO reportDTO) {
        try {
            // Récupérer l'email de l'utilisateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String reporterEmail = auth.getName();
            
            // Ajouter des vérifications explicites pour éviter les warnings
            Long reportedUserId = reportDTO.getReportedUserId();
            ReportType reportType = reportDTO.getReportType();
            String reason = reportDTO.getReason();
            
            // Créer des copies locales pour éviter les warnings
            Long reportedUserIdParam = reportedUserId;
            ReportType reportTypeParam = reportType;
            String reasonParam = reason;
            
            if (reportedUserIdParam == null || reporterEmail == null || reportTypeParam == null || reasonParam == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Tous les champs du signalement sont obligatoires");
            }
            
            // Solution temporaire : utiliser l'email pour trouver l'utilisateur directement
            User reporter = userRepository.findByEmail(reporterEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur connecté non trouvé"));
            
            // Créer une copie locale pour éviter le warning de nullité
            Long reporterId = reporter.getId();
            if (reporterId == null) {
                throw new IllegalArgumentException("L'ID du reporter ne peut pas être null");
            }
            Long reporterIdCopy = reporterId;
            
            UserReport report = userReportService.createReport(
                reportedUserIdParam,
                reporterIdCopy,
                reportTypeParam,
                reasonParam
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"success\": true, \"message\": \"Signalement créé avec succès. ID: " + report.getId() + "\", \"reportId\": " + report.getId() + "}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"BAD_REQUEST\", \"message\": \"" + e.getMessage() + "\", \"code\": 400}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la création du signalement: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @SuppressWarnings("nullness")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> getPendingReports() {
        try {
            List<UserReport> reports = userReportService.getPendingReports();
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"reports\": " + reports.size() + " signalements en attente\", \"data\": " + reports + "}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la récupération des signalements en attente: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> getReportsByUser(@PathVariable Long userId) {
        try {
            // Ajouter une vérification explicite pour éviter le warning
            Long userIdParam = userId;
            if (userIdParam == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"BAD_REQUEST\", \"message\": \"L'ID utilisateur ne peut pas être null\", \"code\": 400}");
            }
            
            List<UserReport> reports = userReportService.getReportsByUserId(userIdParam);
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"reports\": " + reports.size() + " signalements trouvés\", \"userId\": " + userIdParam + ", \"data\": " + reports + "}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la récupération des signalements: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @SuppressWarnings("nullness")
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> getAllReports() {
        try {
            System.out.println("UserReportController: getAllReports() called");
            List<UserReportResponseDTO> reports = userReportService.getAllReportsDTO();
            System.out.println("UserReportController: Found " + reports.size() + " reports");
            System.out.println("UserReportController: Reports data: " + reports);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            System.err.println("UserReportController: Error in getAllReports(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la récupération de tous les signalements: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @PutMapping("/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> resolveReport(
            @PathVariable Long reportId,
            @RequestParam ReportStatus status,
            @RequestParam(required = false) String adminNotes
    ) {
        try {
            System.out.println("UserReportController: resolveReport() called");
            System.out.println("UserReportController: reportId = " + reportId);
            System.out.println("UserReportController: status = " + status);
            System.out.println("UserReportController: adminNotes = " + adminNotes);
            
            // Récupérer l'email de l'administrateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminEmail = auth.getName();
            System.out.println("UserReportController: adminEmail = " + adminEmail);
            
            // Récupérer l'administrateur par email
            User admin = userRepository.findByEmail(adminEmail)
                    .orElseThrow(() -> new RuntimeException("Administrateur non trouvé"));
            Long adminId = admin.getId();
            System.out.println("UserReportController: adminId = " + adminId);
            
            // Ajouter des vérifications explicites pour éviter les warnings
            Long reportIdParam = reportId;
            Long adminIdParam = adminId;
            ReportStatus statusParam = status;
            
            // Créer des copies locales pour éviter les warnings
            Long reportIdCopy = reportIdParam;
            Long adminIdCopy = adminIdParam;
            ReportStatus statusCopy = statusParam;
            
            if (reportIdCopy == null || adminIdCopy == null || statusCopy == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"BAD_REQUEST\", \"message\": \"L'ID du signalement, l'ID admin et le statut sont obligatoires\", \"code\": 400}");
            }
            
            UserReport resolvedReport = userReportService.resolveReport(
                reportIdCopy, adminIdCopy, statusCopy, adminNotes
            );
            
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"message\": \"Signalement résolu avec succès. Status: " + resolvedReport.getStatus() + "\", \"reportId\": " + resolvedReport.getId() + ", \"status\": \"" + resolvedReport.getStatus() + "\"}");
        } catch (IllegalArgumentException e) {
            System.err.println("UserReportController: IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"BAD_REQUEST\", \"message\": \"" + e.getMessage() + "\", \"code\": 400}");
        } catch (Exception e) {
            System.err.println("UserReportController: Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la résolution du signalement: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
}

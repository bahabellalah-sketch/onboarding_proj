package com.onboarding.controller;

import com.onboarding.service.AnalyticsService;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                // If it's a Spring Security User object, find the actual User entity
                String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            }
        }
        throw new RuntimeException("User not authenticated");
    }

    /**
     * Get global progress metrics
     */
    @GetMapping("/global-progress")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<AnalyticsService.GlobalProgressMetrics> getGlobalProgress() {
        try {
            User currentUser = getCurrentUser();
            AnalyticsService.GlobalProgressMetrics metrics = analyticsService.getGlobalProgressMetrics(currentUser);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get overdue onboardings
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AnalyticsService.OverdueOnboarding>> getOverdueOnboardings() {
        try {
            User currentUser = getCurrentUser();
            List<AnalyticsService.OverdueOnboarding> overdue = analyticsService.getOverdueOnboardings(currentUser);
            return ResponseEntity.ok(overdue);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get filtered assignments
     */
    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AnalyticsService.AssignmentAnalytics>> getFilteredAssignments(
            @RequestBody AnalyticsService.AssignmentFilters filters) {
        try {
            User currentUser = getCurrentUser();
            List<AnalyticsService.AssignmentAnalytics> assignments = analyticsService.getFilteredAssignments(filters, currentUser);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get department statistics
     */
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Map<String, AnalyticsService.DepartmentStats>> getDepartmentStats() {
        try {
            User currentUser = getCurrentUser();
            Map<String, AnalyticsService.DepartmentStats> stats = analyticsService.getDepartmentStats(currentUser);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get real-time metrics
     */
    @GetMapping("/realtime")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<AnalyticsService.RealTimeMetrics> getRealTimeMetrics() {
        try {
            User currentUser = getCurrentUser();
            AnalyticsService.RealTimeMetrics metrics = analyticsService.getRealTimeMetrics(currentUser);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get analytics summary (combined endpoint for dashboard)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<AnalyticsSummary> getAnalyticsSummary() {
        try {
            User currentUser = getCurrentUser();
            AnalyticsService.GlobalProgressMetrics globalProgress = analyticsService.getGlobalProgressMetrics(currentUser);
            List<AnalyticsService.OverdueOnboarding> overdue = analyticsService.getOverdueOnboardings(currentUser);
            Map<String, AnalyticsService.DepartmentStats> departmentStats = analyticsService.getDepartmentStats(currentUser);
            AnalyticsService.RealTimeMetrics realTime = analyticsService.getRealTimeMetrics(currentUser);

            AnalyticsSummary summary = new AnalyticsSummary(
                    globalProgress,
                    overdue,
                    departmentStats,
                    realTime
            );

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get available filter options
     */
    @GetMapping("/filter-options")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<FilterOptions> getFilterOptions() {
        try {
            User currentUser = getCurrentUser();
            FilterOptions options = analyticsService.getFilterOptions(currentUser);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // DTO for combined summary
    public static class AnalyticsSummary {
        private AnalyticsService.GlobalProgressMetrics globalProgress;
        private List<AnalyticsService.OverdueOnboarding> overdueOnboardings;
        private Map<String, AnalyticsService.DepartmentStats> departmentStats;
        private AnalyticsService.RealTimeMetrics realTimeMetrics;

        public AnalyticsSummary() {}

        public AnalyticsSummary(AnalyticsService.GlobalProgressMetrics globalProgress,
                               List<AnalyticsService.OverdueOnboarding> overdueOnboardings,
                               Map<String, AnalyticsService.DepartmentStats> departmentStats,
                               AnalyticsService.RealTimeMetrics realTimeMetrics) {
            this.globalProgress = globalProgress;
            this.overdueOnboardings = overdueOnboardings;
            this.departmentStats = departmentStats;
            this.realTimeMetrics = realTimeMetrics;
        }

        // Getters and Setters
        public AnalyticsService.GlobalProgressMetrics getGlobalProgress() { return globalProgress; }
        public void setGlobalProgress(AnalyticsService.GlobalProgressMetrics globalProgress) { this.globalProgress = globalProgress; }
        public List<AnalyticsService.OverdueOnboarding> getOverdueOnboardings() { return overdueOnboardings; }
        public void setOverdueOnboardings(List<AnalyticsService.OverdueOnboarding> overdueOnboardings) { this.overdueOnboardings = overdueOnboardings; }
        public Map<String, AnalyticsService.DepartmentStats> getDepartmentStats() { return departmentStats; }
        public void setDepartmentStats(Map<String, AnalyticsService.DepartmentStats> departmentStats) { this.departmentStats = departmentStats; }
        public AnalyticsService.RealTimeMetrics getRealTimeMetrics() { return realTimeMetrics; }
        public void setRealTimeMetrics(AnalyticsService.RealTimeMetrics realTimeMetrics) { this.realTimeMetrics = realTimeMetrics; }
    }

    public static class FilterOptions {
        private List<String> statusOptions;
        private List<String> departmentOptions;
        private List<CollaboratorOption> collaboratorOptions;

        public FilterOptions() {}

        public FilterOptions(List<String> statusOptions, List<String> departmentOptions, 
                           List<CollaboratorOption> collaboratorOptions) {
            this.statusOptions = statusOptions;
            this.departmentOptions = departmentOptions;
            this.collaboratorOptions = collaboratorOptions;
        }

        // Getters and Setters
        public List<String> getStatusOptions() { return statusOptions; }
        public void setStatusOptions(List<String> statusOptions) { this.statusOptions = statusOptions; }
        public List<String> getDepartmentOptions() { return departmentOptions; }
        public void setDepartmentOptions(List<String> departmentOptions) { this.departmentOptions = departmentOptions; }
        public List<CollaboratorOption> getCollaboratorOptions() { return collaboratorOptions; }
        public void setCollaboratorOptions(List<CollaboratorOption> collaboratorOptions) { this.collaboratorOptions = collaboratorOptions; }
    }

    public static class CollaboratorOption {
        private Long id;
        private String name;
        private String department;

        public CollaboratorOption() {}

        public CollaboratorOption(Long id, String name, String department) {
            this.id = id;
            this.name = name;
            this.department = department;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
    }
}

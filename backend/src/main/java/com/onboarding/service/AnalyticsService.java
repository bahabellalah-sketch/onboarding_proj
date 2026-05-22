package com.onboarding.service;

import com.onboarding.entity.Assignment;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.StatutAssignment;
import com.onboarding.entity.StatutChecklist;
import com.onboarding.entity.User;
import com.onboarding.controller.AnalyticsController;
import com.onboarding.repository.AssignmentRepository;
import com.onboarding.repository.ChecklistRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Helper method to filter assignments based on user role
     * Administrators see all assignments, Managers only see their managed employees' assignments
     */
    private List<Assignment> getFilteredAssignments(User currentUser) {
        System.out.println("DEBUG: getFilteredAssignments called for user: " + currentUser.getId() + " (" + currentUser.getNom() + " " + currentUser.getPrenom() + ") with role: " + currentUser.getRole().name());
        
        if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
            System.out.println("DEBUG: User is ADMINISTRATEUR, returning all assignments");
            return assignmentRepository.findAll();
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            // Get the manager with their managed employees properly loaded
            User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                    .orElse(currentUser);
            
            // Get all employees managed by this manager
            List<User> managedEmployees = managerWithEmployees.getManagedEmployees();
            System.out.println("DEBUG: Managed employees from relationship: " + (managedEmployees != null ? managedEmployees.size() : 0));
            
            if (managedEmployees == null || managedEmployees.isEmpty()) {
                // Fallback: try to find employees by manager_id directly
                managedEmployees = userRepository.findByManagerId(currentUser.getId());
                System.out.println("DEBUG: Managed employees from direct query: " + managedEmployees.size());
                
                if (managedEmployees.isEmpty()) {
                    System.out.println("DEBUG: No managed employees found, returning empty list");
                    return Collections.emptyList();
                }
            }
            
            // Debug: Print managed employee details
            for (User emp : managedEmployees) {
                System.out.println("DEBUG: Managed employee: " + emp.getId() + " (" + emp.getNom() + " " + emp.getPrenom() + "), manager: " + (emp.getManager() != null ? emp.getManager().getId() : "null"));
            }
            
            // Get assignments for managed employees
            List<Long> managedEmployeeIds = managedEmployees.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            
            List<Assignment> assignments = assignmentRepository.findByUserIdIn(managedEmployeeIds);
            System.out.println("DEBUG: Found " + assignments.size() + " assignments for managed employees");
            
            // Debug: Print assignment details
            for (Assignment assignment : assignments) {
                System.out.println("DEBUG: Assignment: " + assignment.getId() + " for user: " + assignment.getUser().getId() + " (" + assignment.getUser().getNom() + " " + assignment.getUser().getPrenom() + ")");
            }
            
            return assignments;
        }
        System.out.println("DEBUG: User role not recognized, returning empty list");
        return Collections.emptyList();
    }

    /**
     * Calculate global progress rate across all assignments
     * For managers, only shows assignments of their managed employees
     */
    public GlobalProgressMetrics getGlobalProgressMetrics(User currentUser) {
        List<Assignment> allAssignments = getFilteredAssignments(currentUser);
        
        if (allAssignments.isEmpty()) {
            return new GlobalProgressMetrics();
        }

        long totalAssignments = allAssignments.size();
        long completedAssignments = allAssignments.stream()
                .filter(a -> a.getStatut() == StatutAssignment.TERMINE)
                .count();
        
        long inProgressAssignments = allAssignments.stream()
                .filter(a -> a.getStatut() == StatutAssignment.EN_COURS)
                .count();
        
        long overdueAssignments = allAssignments.stream()
                .filter(a -> a.getStatut() == StatutAssignment.EN_RETARD)
                .count();
        
        long waitingAssignments = allAssignments.stream()
                .filter(a -> a.getStatut() == StatutAssignment.EN_ATTENTE)
                .count();

        // Calculate average completion percentage
        double avgCompletionPercentage = calculateAverageCompletion(allAssignments);

        return new GlobalProgressMetrics(
                totalAssignments,
                completedAssignments,
                inProgressAssignments,
                overdueAssignments,
                waitingAssignments,
                avgCompletionPercentage
        );
    }

    /**
     * Identify overdue onboardings
     * For managers, only shows overdue assignments of their managed employees
     */
    public List<OverdueOnboarding> getOverdueOnboardings(User currentUser) {
        LocalDate now = LocalDate.now();
        
        return getFilteredAssignments(currentUser).stream()
                .filter(a -> a.getDateFinPrevisionnelle() != null && 
                           a.getDateFinPrevisionnelle().isBefore(now) &&
                           a.getStatut() != StatutAssignment.TERMINE)
                .map(this::createOverdueOnboarding)
                .sorted((o1, o2) -> o2.getDaysOverdue().compareTo(o1.getDaysOverdue()))
                .collect(Collectors.toList());
    }

    /**
     * Get assignments with filtering capabilities
     * For managers, only shows assignments of their managed employees
     */
    public List<AssignmentAnalytics> getFilteredAssignments(AssignmentFilters filters, User currentUser) {
        List<Assignment> assignments = getFilteredAssignments(currentUser);
        
        // Apply filters
        if (filters.getStatus() != null && !filters.getStatus().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> filters.getStatus().contains(a.getStatut().name()))
                    .collect(Collectors.toList());
        }
        
        if (filters.getDepartment() != null && !filters.getDepartment().isEmpty()) {
            assignments = assignments.stream()
                    .filter(a -> {
                        User user = a.getUser();
                        return user != null && filters.getDepartment().equals(user.getDepartement());
                    })
                    .collect(Collectors.toList());
        }
        
        if (filters.getCollaboratorId() != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getUser() != null && a.getUser().getId().equals(filters.getCollaboratorId()))
                    .collect(Collectors.toList());
        }
        
        if (filters.getStartDate() != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getDateDebut() != null && !a.getDateDebut().isBefore(filters.getStartDate()))
                    .collect(Collectors.toList());
        }
        
        if (filters.getEndDate() != null) {
            assignments = assignments.stream()
                    .filter(a -> a.getDateDebut() != null && !a.getDateDebut().isAfter(filters.getEndDate()))
                    .collect(Collectors.toList());
        }

        return assignments.stream()
                .map(this::createAssignmentAnalytics)
                .collect(Collectors.toList());
    }

    /**
     * Get department-wise statistics
     * For managers, only shows statistics for their managed employees' departments
     */
    public Map<String, DepartmentStats> getDepartmentStats(User currentUser) {
        Map<String, List<Assignment>> assignmentsByDepartment = getFilteredAssignments(currentUser)
                .stream()
                .filter(a -> a.getUser() != null && a.getUser().getDepartement() != null)
                .collect(Collectors.groupingBy(a -> a.getUser().getDepartement()));

        Map<String, DepartmentStats> stats = new HashMap<>();
        
        for (Map.Entry<String, List<Assignment>> entry : assignmentsByDepartment.entrySet()) {
            String department = entry.getKey();
            List<Assignment> deptAssignments = entry.getValue();
            
            DepartmentStats deptStats = new DepartmentStats();
            deptStats.setDepartment(department);
            deptStats.setTotalAssignments(deptAssignments.size());
            deptStats.setCompletedAssignments((int) deptAssignments.stream()
                    .filter(a -> a.getStatut() == StatutAssignment.TERMINE)
                    .count());
            deptStats.setOverdueAssignments((int) deptAssignments.stream()
                    .filter(a -> a.getStatut() == StatutAssignment.EN_RETARD)
                    .count());
            deptStats.setAverageCompletion(calculateAverageCompletion(deptAssignments));
            
            stats.put(department, deptStats);
        }
        
        return stats;
    }

    /**
     * Get real-time metrics for dashboard
     * For managers, only shows metrics for their managed employees
     */
    public RealTimeMetrics getRealTimeMetrics(User currentUser) {
        LocalDateTime now = LocalDateTime.now();
        
        // Active users today - for managers, count their managed employees
        List<Assignment> userAssignments = getFilteredAssignments(currentUser);
        long activeUsersToday = userAssignments.stream()
                .filter(a -> a.getUser() != null && 
                           a.getUser().getDateModification() != null && 
                           a.getUser().getDateModification().toLocalDate().equals(now.toLocalDate()))
                .map(Assignment::getUser)
                .distinct()
                .count();
        
        // Assignments created this week
        LocalDate weekAgo = now.toLocalDate().minus(7, ChronoUnit.DAYS);
        long assignmentsThisWeek = userAssignments.stream()
                .filter(a -> a.getDateCreation() != null && 
                           a.getDateCreation().isAfter(weekAgo))
                .count();
        
        // Checklists completed today
        List<Long> userIds = userAssignments.stream()
                .filter(a -> a.getUser() != null)
                .map(a -> a.getUser().getId())
                .distinct()
                .collect(Collectors.toList());
        
        long checklistsCompletedToday = checklistRepository.findAll().stream()
                .filter(c -> c.getDateRealisation() != null && 
                           c.getDateRealisation().equals(now.toLocalDate()) &&
                           c.getStatut() == StatutChecklist.TERMINE &&
                           userIds.contains(c.getAssignment().getUser().getId()))
                .count();

        return new RealTimeMetrics(activeUsersToday, assignmentsThisWeek, checklistsCompletedToday, now);
    }

    /**
     * Get available filter options for analytics
     * For managers, only shows their managed employees as collaborator options
     */
    public AnalyticsController.FilterOptions getFilterOptions(User currentUser) {
        // Status options
        List<String> statusOptions = Arrays.asList(
                "EN_ATTENTE", "EN_COURS", "TERMINE", "EN_RETARD", "EN_PAUSE", "ANNULE"
        );
        
        // Department options - for managers, only show departments of their managed employees
        List<String> departmentOptions;
        List<AnalyticsController.CollaboratorOption> collaboratorOptions;
        
        if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
            departmentOptions = userRepository.findAll().stream()
                    .filter(u -> u.getDepartement() != null)
                    .map(User::getDepartement)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            collaboratorOptions = userRepository.findAll().stream()
                    .filter(u -> u.getRole().name().equals("COLLABORATEUR"))
                    .map(u -> new AnalyticsController.CollaboratorOption(u.getId(), u.getNom() + " " + u.getPrenom(), u.getDepartement()))
                    .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                    .collect(Collectors.toList());
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            // For managers, only show departments and collaborators of their managed employees
            User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                    .orElse(currentUser);
            List<User> managedEmployees = managerWithEmployees.getManagedEmployees();
            
            if (managedEmployees == null || managedEmployees.isEmpty()) {
                // Fallback: try to find employees by manager_id directly
                managedEmployees = userRepository.findByManagerId(currentUser.getId());
                if (managedEmployees.isEmpty()) {
                    departmentOptions = Collections.emptyList();
                    collaboratorOptions = Collections.emptyList();
                } else {
                    departmentOptions = managedEmployees.stream()
                            .filter(u -> u.getDepartement() != null)
                            .map(User::getDepartement)
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    
                    collaboratorOptions = managedEmployees.stream()
                            .filter(u -> u.getRole().name().equals("COLLABORATEUR"))
                            .map(u -> new AnalyticsController.CollaboratorOption(u.getId(), u.getNom() + " " + u.getPrenom(), u.getDepartement()))
                            .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                            .collect(Collectors.toList());
                }
            } else {
                departmentOptions = managedEmployees.stream()
                        .filter(u -> u.getDepartement() != null)
                        .map(User::getDepartement)
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
                
                collaboratorOptions = managedEmployees.stream()
                        .filter(u -> u.getRole().name().equals("COLLABORATEUR"))
                        .map(u -> new AnalyticsController.CollaboratorOption(u.getId(), u.getNom() + " " + u.getPrenom(), u.getDepartement()))
                        .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
                        .collect(Collectors.toList());
            }
        } else {
            departmentOptions = Collections.emptyList();
            collaboratorOptions = Collections.emptyList();
        }
        
        return new AnalyticsController.FilterOptions(statusOptions, departmentOptions, collaboratorOptions);
    }

    // Helper methods
    private double calculateAverageCompletion(List<Assignment> assignments) {
        if (assignments.isEmpty()) return 0.0;
        
        double totalCompletion = assignments.stream()
                .mapToDouble(this::calculateAssignmentCompletion)
                .sum();
        
        return totalCompletion / assignments.size();
    }

    private double calculateAssignmentCompletion(Assignment assignment) {
        List<Checklist> checklists = checklistRepository.findByAssignmentId(assignment.getId());
        
        if (checklists.isEmpty()) return 0.0;
        
        long completedChecklists = checklists.stream()
                .filter(c -> c.getStatut() != null && c.getStatut() == StatutChecklist.TERMINE)
                .count();
        
        return (double) completedChecklists / checklists.size() * 100;
    }

    private OverdueOnboarding createOverdueOnboarding(Assignment assignment) {
        long daysOverdue = ChronoUnit.DAYS.between(
                assignment.getDateFinPrevisionnelle(), 
                LocalDate.now()
        );
        
        return new OverdueOnboarding(
                assignment.getId(),
                assignment.getUser().getPrenom() + " " + assignment.getUser().getNom(),
                assignment.getUser().getDepartement(),
                assignment.getDateFinPrevisionnelle().atStartOfDay(),
                daysOverdue,
                calculateAssignmentCompletion(assignment)
        );
    }

    private AssignmentAnalytics createAssignmentAnalytics(Assignment assignment) {
        return new AssignmentAnalytics(
                assignment.getId(),
                assignment.getUser().getPrenom() + " " + assignment.getUser().getNom(),
                assignment.getUser().getDepartement(),
                assignment.getStatut().name(),
                assignment.getDateDebut().atStartOfDay(),
                assignment.getDateFinPrevisionnelle().atStartOfDay(),
                calculateAssignmentCompletion(assignment),
                checklistRepository.findByAssignmentId(assignment.getId()).size()
        );
    }

    // DTO Classes
    public static class GlobalProgressMetrics {
        private long totalAssignments;
        private long completedAssignments;
        private long inProgressAssignments;
        private long overdueAssignments;
        private long waitingAssignments;
        private double averageCompletionPercentage;

        public GlobalProgressMetrics() {}

        public GlobalProgressMetrics(long totalAssignments, long completedAssignments, 
                                   long inProgressAssignments, long overdueAssignments, 
                                   long waitingAssignments, double averageCompletionPercentage) {
            this.totalAssignments = totalAssignments;
            this.completedAssignments = completedAssignments;
            this.inProgressAssignments = inProgressAssignments;
            this.overdueAssignments = overdueAssignments;
            this.waitingAssignments = waitingAssignments;
            this.averageCompletionPercentage = averageCompletionPercentage;
        }

        // Getters and Setters
        public long getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }
        public long getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(long completedAssignments) { this.completedAssignments = completedAssignments; }
        public long getInProgressAssignments() { return inProgressAssignments; }
        public void setInProgressAssignments(long inProgressAssignments) { this.inProgressAssignments = inProgressAssignments; }
        public long getOverdueAssignments() { return overdueAssignments; }
        public void setOverdueAssignments(long overdueAssignments) { this.overdueAssignments = overdueAssignments; }
        public long getWaitingAssignments() { return waitingAssignments; }
        public void setWaitingAssignments(long waitingAssignments) { this.waitingAssignments = waitingAssignments; }
        public double getAverageCompletionPercentage() { return averageCompletionPercentage; }
        public void setAverageCompletionPercentage(double averageCompletionPercentage) { this.averageCompletionPercentage = averageCompletionPercentage; }
    }

    public static class OverdueOnboarding {
        private Long assignmentId;
        private String collaboratorName;
        private String department;
        private LocalDateTime dueDate;
        private Long daysOverdue;
        private Double completionPercentage;

        public OverdueOnboarding() {}

        public OverdueOnboarding(Long assignmentId, String collaboratorName, String department, 
                                LocalDateTime dueDate, Long daysOverdue, Double completionPercentage) {
            this.assignmentId = assignmentId;
            this.collaboratorName = collaboratorName;
            this.department = department;
            this.dueDate = dueDate;
            this.daysOverdue = daysOverdue;
            this.completionPercentage = completionPercentage;
        }

        // Getters and Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getCollaboratorName() { return collaboratorName; }
        public void setCollaboratorName(String collaboratorName) { this.collaboratorName = collaboratorName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        public Long getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(Long daysOverdue) { this.daysOverdue = daysOverdue; }
        public Double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
    }

    public static class AssignmentFilters {
        private List<String> status;
        private String department;
        private Long collaboratorId;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;

        // Getters and Setters
        public List<String> getStatus() { return status; }
        public void setStatus(List<String> status) { this.status = status; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Long getCollaboratorId() { return collaboratorId; }
        public void setCollaboratorId(Long collaboratorId) { this.collaboratorId = collaboratorId; }
        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }
        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
    }

    public static class AssignmentAnalytics {
        private Long assignmentId;
        private String collaboratorName;
        private String department;
        private String status;
        private LocalDateTime startDate;
        private LocalDateTime dueDate;
        private Double completionPercentage;
        private Integer totalChecklists;

        public AssignmentAnalytics() {}

        public AssignmentAnalytics(Long assignmentId, String collaboratorName, String department, 
                                  String status, LocalDateTime startDate, LocalDateTime dueDate, 
                                  Double completionPercentage, Integer totalChecklists) {
            this.assignmentId = assignmentId;
            this.collaboratorName = collaboratorName;
            this.department = department;
            this.status = status;
            this.startDate = startDate;
            this.dueDate = dueDate;
            this.completionPercentage = completionPercentage;
            this.totalChecklists = totalChecklists;
        }

        // Getters and Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getCollaboratorName() { return collaboratorName; }
        public void setCollaboratorName(String collaboratorName) { this.collaboratorName = collaboratorName; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
        public Double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }
        public Integer getTotalChecklists() { return totalChecklists; }
        public void setTotalChecklists(Integer totalChecklists) { this.totalChecklists = totalChecklists; }
    }

    public static class DepartmentStats {
        private String department;
        private int totalAssignments;
        private int completedAssignments;
        private int overdueAssignments;
        private double averageCompletion;

        // Getters and Setters
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }
        public int getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }
        public int getOverdueAssignments() { return overdueAssignments; }
        public void setOverdueAssignments(int overdueAssignments) { this.overdueAssignments = overdueAssignments; }
        public double getAverageCompletion() { return averageCompletion; }
        public void setAverageCompletion(double averageCompletion) { this.averageCompletion = averageCompletion; }
    }

    public static class RealTimeMetrics {
        private long activeUsersToday;
        private long assignmentsCreatedThisWeek;
        private long checklistsCompletedToday;
        private LocalDateTime lastUpdated;

        public RealTimeMetrics() {}

        public RealTimeMetrics(long activeUsersToday, long assignmentsCreatedThisWeek, 
                              long checklistsCompletedToday, LocalDateTime lastUpdated) {
            this.activeUsersToday = activeUsersToday;
            this.assignmentsCreatedThisWeek = assignmentsCreatedThisWeek;
            this.checklistsCompletedToday = checklistsCompletedToday;
            this.lastUpdated = lastUpdated;
        }

        // Getters and Setters
        public long getActiveUsersToday() { return activeUsersToday; }
        public void setActiveUsersToday(long activeUsersToday) { this.activeUsersToday = activeUsersToday; }
        public long getAssignmentsCreatedThisWeek() { return assignmentsCreatedThisWeek; }
        public void setAssignmentsCreatedThisWeek(long assignmentsCreatedThisWeek) { this.assignmentsCreatedThisWeek = assignmentsCreatedThisWeek; }
        public long getChecklistsCompletedToday() { return checklistsCompletedToday; }
        public void setChecklistsCompletedToday(long checklistsCompletedToday) { this.checklistsCompletedToday = checklistsCompletedToday; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}

package com.onboarding.controller;

import com.onboarding.dto.AssignmentDTO;
import com.onboarding.dto.ChecklistDTO;
import com.onboarding.entity.Assignment;
import com.onboarding.entity.User;
import com.onboarding.service.AssignmentService;
import com.onboarding.service.UserService;
import com.onboarding.repository.UserRepository;
import com.onboarding.repository.AssignmentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assignments")
// @CrossOrigin handled globally in SecurityConfig
public class AssignmentController {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
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
     * Helper method to check if current user can manage the target user
     */
    private boolean canManageUser(User currentUser, User targetUser) {
        if (targetUser == null) {
            return false;
        }
        // Administrators can assign to any collaborator
        if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
            return targetUser.getRole().name().equals("COLLABORATEUR");
        }
        // Managers: only direct reports (manager_id), with DB fallback if relation not loaded
        if (currentUser.getRole().name().equals("MANAGER")) {
            if (!targetUser.getRole().name().equals("COLLABORATEUR")) {
                return false;
            }
            User target = userRepository.findByIdWithManager(targetUser.getId()).orElse(targetUser);
            if (target.getManager() != null
                    && currentUser.getId().equals(target.getManager().getId())) {
                return true;
            }
            return userRepository.findByManagerId(currentUser.getId()).stream()
                    .anyMatch(u -> u.getId().equals(target.getId()));
        }
        return false;
    }
    
    // Récupérer toutes les assignations (filtrées par rôle)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AssignmentDTO>> getAllAssignments() {
        try {
            User currentUser = getCurrentUser();
            System.out.println("DEBUG: getAllAssignments called for user: " + currentUser.getId() + " (" + currentUser.getNom() + " " + currentUser.getPrenom() + ") with role: " + currentUser.getRole().name());
            
            List<AssignmentDTO> assignments;
            
            if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
                // Administrateurs voient toutes les assignations
                System.out.println("DEBUG: User is ADMINISTRATEUR, getting all assignments");
                assignments = assignmentService.getAllAssignments();
            } else if (currentUser.getRole().name().equals("MANAGER")) {
                // Managers voient seulement les assignations de leurs collaborateurs
                System.out.println("DEBUG: User is MANAGER, getting assignments by manager");
                assignments = assignmentService.getAssignmentsByManager(currentUser.getId());
            } else {
                System.out.println("DEBUG: User role not recognized, returning empty list");
                assignments = List.of();
            }
            
            System.out.println("DEBUG: Returning " + assignments.size() + " assignments");
            for (AssignmentDTO assignment : assignments) {
                System.out.println("DEBUG: Assignment DTO: " + assignment.getId() + " for user: " + assignment.getUserId() + " (" + assignment.getUserName() + ")");
            }
            
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            System.out.println("DEBUG: Error in getAllAssignments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Récupérer les assignations de l'utilisateur connecté (pour les collaborateurs)
    @GetMapping("/my-assignments")
    public ResponseEntity<List<AssignmentDTO>> getMyAssignments() {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsForCurrentUser();
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Vérifier et mettre à jour les parcours en retard (admin uniquement)
    @PostMapping("/check-overdue")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<String> checkOverdueAssignments() {
        try {
            assignmentService.checkAndUpdateOverdueAssignments();
            return ResponseEntity.ok("Vérification des parcours en retard terminée avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la vérification des parcours en retard: " + e.getMessage());
        }
    }
    
    // Récupérer une assignation par ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<AssignmentDTO> getAssignmentById(@PathVariable Long id) {
        try {
            Optional<AssignmentDTO> assignment = assignmentService.getAssignmentById(id);
            if (assignment.isPresent()) {
                return ResponseEntity.ok(assignment.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Récupérer les assignations par utilisateur
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByUserId(@PathVariable Long userId) {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByUserId(userId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Récupérer les assignations par parcours
    @GetMapping("/parcours/{parcoursId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsByParcoursId(@PathVariable Long parcoursId) {
        try {
            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByParcoursId(parcoursId);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Récupérer les assignations en retard (filtrées par rôle)
    @GetMapping("/en-retard")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsEnRetard() {
        try {
            User currentUser = getCurrentUser();
            List<AssignmentDTO> assignments;
            
            if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
                // Administrateurs voient toutes les assignations en retard
                assignments = assignmentService.getAssignmentsEnRetard();
            } else if (currentUser.getRole().name().equals("MANAGER")) {
                // Managers voient seulement les assignations en retard de leurs collaborateurs
                assignments = assignmentService.getAssignmentsEnRetardByManager(currentUser.getId());
            } else {
                assignments = List.of();
            }
            
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Récupérer les assignations proches de l'échéance (filtrées par rôle)
    @GetMapping("/echeance-proche")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<AssignmentDTO>> getAssignmentsProchesEcheance() {
        try {
            User currentUser = getCurrentUser();
            List<AssignmentDTO> assignments;
            
            if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
                // Administrateurs voient toutes les assignations proches de l'échéance
                assignments = assignmentService.getAssignmentsProchesEcheance();
            } else if (currentUser.getRole().name().equals("MANAGER")) {
                // Managers voient seulement les assignations proches de l'échéance de leurs collaborateurs
                assignments = assignmentService.getAssignmentsProchesEcheanceByManager(currentUser.getId());
            } else {
                assignments = List.of();
            }
            
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Créer une nouvelle assignation
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<AssignmentDTO> createAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            User currentUser = getCurrentUser();
            
            // Vérifier que le manager peut créer une assignation pour cet utilisateur
            if (assignmentDTO.getUserId() != null) {
                User targetUser = userRepository.findById(assignmentDTO.getUserId())
                        .orElse(null);
                
                if (targetUser == null) {
                    return ResponseEntity.badRequest().build();
                }
                
                if (!canManageUser(currentUser, targetUser)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(null);
                }
            }
            
            String assignePar = currentUser.getNom() + " " + currentUser.getPrenom();
            
            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO, assignePar);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Mettre à jour une assignation
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id, @Valid @RequestBody AssignmentDTO assignmentDTO) {
        try {
            AssignmentDTO updatedAssignment = assignmentService.updateAssignment(id, assignmentDTO);
            if (updatedAssignment != null) {
                return ResponseEntity.ok(updatedAssignment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Mettre à jour le statut d'une assignation
    @PatchMapping("/{id}/statut")
    public ResponseEntity<AssignmentDTO> updateStatutAssignment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statut = request.get("statut");
            AssignmentDTO updatedAssignment = assignmentService.updateStatutAssignment(id, statut);
            if (updatedAssignment != null) {
                return ResponseEntity.ok(updatedAssignment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Mettre à jour l'avancement
    @PatchMapping("/{id}/avancement")
    public ResponseEntity<AssignmentDTO> updateAvancement(@PathVariable Long id, @RequestBody Integer pourcentage) {
        try {
            AssignmentDTO updatedAssignment = assignmentService.updateAvancement(id, pourcentage);
            if (updatedAssignment != null) {
                return ResponseEntity.ok(updatedAssignment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Supprimer une assignation
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            System.out.println("DEBUG: deleteAssignment called - Current user: " + currentUser.getId() + " (" + currentUser.getNom() + " " + currentUser.getPrenom() + ") role: " + currentUser.getRole().name());
            System.out.println("DEBUG: Attempting to delete assignment ID: " + id);
            
            // Get the assignment to check if the manager can delete it
            Optional<AssignmentDTO> assignmentOpt = assignmentService.getAssignmentById(id);
            if (assignmentOpt.isEmpty()) {
                System.out.println("DEBUG: Assignment not found");
                return ResponseEntity.notFound().build();
            }
            
            AssignmentDTO assignment = assignmentOpt.get();
            System.out.println("DEBUG: Assignment found - User ID: " + assignment.getUserId() + " User Name: " + assignment.getUserName());
            
            // For managers, check if they can manage the user of this assignment
            if (currentUser.getRole().name().equals("MANAGER")) {
                System.out.println("DEBUG: User is MANAGER, checking access control");
                User targetUser = userRepository.findById(assignment.getUserId())
                        .orElse(null);
                
                if (targetUser == null) {
                    System.out.println("DEBUG: Target user not found");
                    return ResponseEntity.notFound().build();
                }
                
                System.out.println("DEBUG: Target user: " + targetUser.getId() + " (" + targetUser.getNom() + " " + targetUser.getPrenom() + ")");
                System.out.println("DEBUG: Target user manager: " + (targetUser.getManager() != null ? targetUser.getManager().getId() : "null"));
                
                boolean canManage = canManageUser(currentUser, targetUser);
                System.out.println("DEBUG: Can manage user: " + canManage);
                
                if (!canManage) {
                    System.out.println("DEBUG: Manager cannot manage this user - returning 403");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(null);
                }
            }
            
            // Proceed with deletion
            System.out.println("DEBUG: Proceeding with deletion");
            assignmentService.deleteAssignment(id);
            System.out.println("DEBUG: Assignment deleted successfully");
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            System.out.println("DEBUG: Error in deleteAssignment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Endpoint pour assigner un parcours à un collaborateur
    @PostMapping("/assigner")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> assignerParcours(@RequestBody AssignmentRequest request) {
        try {
            User currentUser = getCurrentUser();
            
            // Vérifier que le manager peut assigner un parcours à cet utilisateur
            User targetUser = userRepository.findByIdWithManager(request.getUserId())
                    .orElse(null);
            
            if (targetUser == null) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("message", "Collaborateur introuvable"));
            }
            
            if (!canManageUser(currentUser, targetUser)) {
                String msg = currentUser.getRole().name().equals("ADMINISTRATEUR")
                        ? "Seuls les comptes collaborateur peuvent recevoir un parcours"
                        : "Vous ne pouvez assigner un parcours qu'aux collaborateurs de votre équipe (manager_id requis)";
                System.out.println("DEBUG: assignerParcours 403 - " + currentUser.getEmail()
                        + " -> " + targetUser.getEmail()
                        + ", manager=" + (targetUser.getManager() != null ? targetUser.getManager().getId() : "null"));
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(java.util.Map.of("message", msg));
            }
            
            AssignmentDTO assignmentDTO = new AssignmentDTO();
            assignmentDTO.setUserId(request.getUserId());
            assignmentDTO.setParcoursId(request.getParcoursId());
            assignmentDTO.setDateDebut(java.time.LocalDate.parse(request.getDateDebut()));
            assignmentDTO.setPourcentageAvancement(0);
            
            String assignePar = currentUser.getNom() + " " + currentUser.getPrenom();
            
            AssignmentDTO createdAssignment = assignmentService.createAssignment(assignmentDTO, assignePar);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAssignment);
        } catch (RuntimeException e) {
            System.err.println("Error assigning parcours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Unexpected error assigning parcours: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Classe interne pour la requête d'assignation
    public static class AssignmentRequest {
        private Long userId;
        private Long parcoursId;
        private String dateDebut;
        
        // Getters and Setters
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public Long getParcoursId() {
            return parcoursId;
        }
        
        public void setParcoursId(Long parcoursId) {
            this.parcoursId = parcoursId;
        }
        
        public String getDateDebut() {
            return dateDebut;
        }
        
        public void setDateDebut(String dateDebut) {
            this.dateDebut = dateDebut;
        }
    }
    
    // Debug endpoint to test manager-employee relationships
    @GetMapping("/debug-manager-relationships")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<String> debugManagerRelationships() {
        try {
            User currentUser = getCurrentUser();
            StringBuilder debugInfo = new StringBuilder();
            
            debugInfo.append("Current User: ").append(currentUser.getId())
                    .append(" (").append(currentUser.getNom()).append(" ").append(currentUser.getPrenom())
                    .append(") Role: ").append(currentUser.getRole().name()).append("\n");
            
            if (currentUser.getRole().name().equals("MANAGER")) {
                // Test relationship loading
                User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                        .orElse(currentUser);
                
                List<User> managedEmployees = managerWithEmployees.getManagedEmployees();
                debugInfo.append("Managed employees from relationship: ").append(managedEmployees != null ? managedEmployees.size() : 0).append("\n");
                
                if (managedEmployees == null || managedEmployees.isEmpty()) {
                    managedEmployees = userRepository.findByManagerId(currentUser.getId());
                    debugInfo.append("Managed employees from direct query: ").append(managedEmployees.size()).append("\n");
                }
                
                for (User emp : managedEmployees) {
                    debugInfo.append("Employee: ").append(emp.getId())
                            .append(" (").append(emp.getNom()).append(" ").append(emp.getPrenom())
                            .append(") Manager ID: ").append(emp.getManager() != null ? emp.getManager().getId() : "null").append("\n");
                }
                
                // Check all assignments
                List<Assignment> allAssignments = assignmentRepository.findAll();
                debugInfo.append("Total assignments in system: ").append(allAssignments.size()).append("\n");
                
                for (Assignment assignment : allAssignments) {
                    debugInfo.append("Assignment: ").append(assignment.getId())
                            .append(" User: ").append(assignment.getUser().getId())
                            .append(" (").append(assignment.getUser().getNom()).append(" ").append(assignment.getUser().getPrenom())
                            .append(") User Manager: ").append(assignment.getUser().getManager() != null ? assignment.getUser().getManager().getId() : "null").append("\n");
                }
                
                // Test the actual filtering logic
                debugInfo.append("\n=== TESTING FILTERING LOGIC ===\n");
                List<Long> managedUserIds = managedEmployees.stream()
                        .map(User::getId)
                        .collect(Collectors.toList());
                debugInfo.append("Managed user IDs: ").append(managedUserIds).append("\n");
                
                List<Assignment> filteredAssignments = assignmentRepository.findByUserIdIn(managedUserIds);
                debugInfo.append("Filtered assignments count: ").append(filteredAssignments.size()).append("\n");
                
                for (Assignment assignment : filteredAssignments) {
                    debugInfo.append("Filtered Assignment: ").append(assignment.getId())
                            .append(" User: ").append(assignment.getUser().getId())
                            .append(" (").append(assignment.getUser().getNom()).append(" ").append(assignment.getUser().getPrenom())
                            .append(")\n");
                }
            }
            
            return ResponseEntity.ok(debugInfo.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // Simple test endpoint to check basic functionality
    @GetMapping("/test-simple")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<String> testSimple() {
        try {
            User currentUser = getCurrentUser();
            return ResponseEntity.ok("Simple test successful. User: " + currentUser.getNom() + " " + currentUser.getPrenom() + ", Role: " + currentUser.getRole().name());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // Test endpoint to check specific assignment deletion without authentication
    @GetMapping("/test-delete/{id}")
    public ResponseEntity<String> testDeleteAssignment(@PathVariable Long id) {
        try {
            StringBuilder result = new StringBuilder();
            
            // Get assignment details
            Optional<AssignmentDTO> assignmentOpt = assignmentService.getAssignmentById(id);
            if (assignmentOpt.isEmpty()) {
                return ResponseEntity.ok("Assignment not found: " + id);
            }
            
            AssignmentDTO assignment = assignmentOpt.get();
            result.append("Assignment ID: ").append(id).append("\n");
            result.append("User ID: ").append(assignment.getUserId()).append("\n");
            result.append("User Name: ").append(assignment.getUserName()).append("\n");
            
            // Get target user details
            User targetUser = userRepository.findById(assignment.getUserId()).orElse(null);
            if (targetUser != null) {
                result.append("Target User: ").append(targetUser.getId()).append(" (").append(targetUser.getNom()).append(" ").append(targetUser.getPrenom()).append(")\n");
                result.append("Target User Manager ID: ").append(targetUser.getManager() != null ? targetUser.getManager().getId() : "null").append("\n");
                if (targetUser.getManager() != null) {
                    result.append("Target User Manager Name: ").append(targetUser.getManager().getNom()).append(" ").append(targetUser.getManager().getPrenom()).append("\n");
                }
            }
            
            // Get all managers
            List<User> managers = userRepository.findByRole(com.onboarding.entity.Role.MANAGER);
            result.append("\nAll Managers:\n");
            for (User manager : managers) {
                result.append("- ").append(manager.getId()).append(" (").append(manager.getNom()).append(" ").append(manager.getPrenom()).append(")\n");
                List<User> managed = userRepository.findByManagerId(manager.getId());
                result.append("  Manages: ").append(managed.size()).append(" employees\n");
                for (User emp : managed) {
                    result.append("    - ").append(emp.getId()).append(" (").append(emp.getNom()).append(" ").append(emp.getPrenom()).append(")\n");
                }
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // Simple test DELETE endpoint to verify if DELETE requests work
    @DeleteMapping("/test-delete-simple/{id}")
    public ResponseEntity<String> testDeleteSimple(@PathVariable Long id) {
        try {
            System.out.println("DEBUG: Simple test DELETE endpoint called for ID: " + id);
            return ResponseEntity.ok("DELETE test successful for ID: " + id);
        } catch (Exception e) {
            System.out.println("DEBUG: Error in simple test DELETE: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    // Public test endpoint that bypasses security completely
    @GetMapping("/public-test/{id}")
    public ResponseEntity<String> publicTestAssignment(@PathVariable Long id) {
        try {
            StringBuilder result = new StringBuilder();
            
            // Get assignment details
            Optional<AssignmentDTO> assignmentOpt = assignmentService.getAssignmentById(id);
            if (assignmentOpt.isEmpty()) {
                return ResponseEntity.ok("Assignment not found: " + id);
            }
            
            AssignmentDTO assignment = assignmentOpt.get();
            result.append("Assignment ID: ").append(id).append("\n");
            result.append("User ID: ").append(assignment.getUserId()).append("\n");
            result.append("User Name: ").append(assignment.getUserName()).append("\n");
            
            // Get target user details
            User targetUser = userRepository.findById(assignment.getUserId()).orElse(null);
            if (targetUser != null) {
                result.append("Target User: ").append(targetUser.getId()).append(" (").append(targetUser.getNom()).append(" ").append(targetUser.getPrenom()).append(")\n");
                result.append("Target User Manager ID: ").append(targetUser.getManager() != null ? targetUser.getManager().getId() : "null").append("\n");
                if (targetUser.getManager() != null) {
                    result.append("Target User Manager Name: ").append(targetUser.getManager().getNom()).append(" ").append(targetUser.getManager().getPrenom()).append("\n");
                }
            }
            
            // Get all managers
            List<User> managers = userRepository.findByRole(com.onboarding.entity.Role.MANAGER);
            result.append("\nAll Managers:\n");
            for (User manager : managers) {
                result.append("- ").append(manager.getId()).append(" (").append(manager.getNom()).append(" ").append(manager.getPrenom()).append(")\n");
                List<User> managed = userRepository.findByManagerId(manager.getId());
                result.append("  Manages: ").append(managed.size()).append(" employees\n");
                for (User emp : managed) {
                    result.append("    - ").append(emp.getId()).append(" (").append(emp.getNom()).append(" ").append(emp.getPrenom()).append(")\n");
                }
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // Endpoint pour migrer les relations manager (à utiliser une seule fois)
    @PostMapping("/migrate-managers")
    public ResponseEntity<String> migrateManagerRelationships() {
        try {
            userService.migrateManagerRelationships();
            return ResponseEntity.ok("Manager relationships migrated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error migrating manager relationships: " + e.getMessage());
        }
    }
    
    // Endpoints pour les checklists
    
    // Récupérer les checklists d'une assignation
    @GetMapping("/{assignmentId}/checklists")
    public ResponseEntity<List<ChecklistDTO>> getChecklistsByAssignmentId(@PathVariable Long assignmentId) {
        try {
            List<ChecklistDTO> checklists = assignmentService.getChecklistsByAssignmentId(assignmentId);
            return ResponseEntity.ok(checklists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Mettre à jour le statut d'une checklist
    @PatchMapping("/checklists/{checklistId}/statut")
    public ResponseEntity<ChecklistDTO> updateChecklistStatut(@PathVariable Long checklistId, @RequestBody Map<String, String> request) {
        try {
            String statut = request.get("statut");
            ChecklistDTO updatedChecklist = assignmentService.updateChecklistStatut(checklistId, statut);
            return ResponseEntity.ok(updatedChecklist);
        } catch (RuntimeException e) {
            // Return validation errors with proper status and message
            System.out.println("DEBUG: Validation error in updateChecklistStatut: " + e.getMessage());
            throw e; // Re-throw to let Spring handle it properly
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected error in updateChecklistStatut: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        System.out.println("DEBUG: Global exception handler - RuntimeException: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}

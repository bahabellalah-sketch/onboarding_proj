package com.onboarding.controller;

import com.onboarding.dto.UserCreationDTO;
import com.onboarding.entity.Role;
import com.onboarding.entity.User;
import com.onboarding.service.UserService;
import com.onboarding.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Endpoint temporaire pour fixer le mot de passe de baha.bellalah@gmail.com
    // @PostMapping("/fix-password")  // DÉSACTIVÉ - Plus nécessaire
    //     // public ResponseEntity<String> fixBahaPassword() { ... }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreationDTO userDTO) {
        try {
            userService.createUser(userDTO, "ADMIN");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Utilisateur créé avec succès. Un email de bienvenue a été envoyé.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }
    
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
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<List<User>> getAllUsers() {
        User currentUser = getCurrentUser();
        System.out.println("DEBUG: getAllUsers called - Current user: " + currentUser.getId() + " (" + currentUser.getNom() + " " + currentUser.getPrenom() + ") role: " + currentUser.getRole().name());
        List<User> users;
        
        if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
            // Administrators see all users
            System.out.println("DEBUG: User is ADMINISTRATEUR, returning all users");
            users = userService.findAll();
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            // Managers see only collaborators they manage (same rule as assignment)
            System.out.println("DEBUG: User is MANAGER, returning managed employees only");
            users = userRepository.findByManagerId(currentUser.getId());
            if (users.isEmpty()) {
                User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                        .orElse(currentUser);
                if (managerWithEmployees.getManagedEmployees() != null
                        && !managerWithEmployees.getManagedEmployees().isEmpty()) {
                    users = managerWithEmployees.getManagedEmployees();
                }
            }
        } else if (currentUser.getRole().name().equals("COLLABORATEUR")) {
            // Collaborators see their team members (other collaborators in same department) and their manager
            System.out.println("DEBUG: User is COLLABORATEUR, returning team members");
            String department = currentUser.getDepartement();
            users = new ArrayList<>();
            
            // Always include the current user
            users.add(currentUser);
            
            if (department != null && !department.isEmpty()) {
                // Get collaborators in same department (excluding current user)
                List<User> departmentCollaborators = userService.findByRole(Role.COLLABORATEUR).stream()
                    .filter(u -> department.equalsIgnoreCase(u.getDepartement()) && !u.getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
                // Get managers in same department
                List<User> departmentManagers = userService.findByRole(Role.MANAGER).stream()
                    .filter(u -> department.equalsIgnoreCase(u.getDepartement()))
                    .collect(Collectors.toList());
                // Add to users list
                users.addAll(departmentManagers);
                users.addAll(departmentCollaborators);
            }
        } else {
            // Other roles get empty list
            System.out.println("DEBUG: User role not recognized, returning empty list");
            users = Collections.emptyList();
        }
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/team-members")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<List<User>> getTeamMembersByManagerId() {
        try {
            User currentUser = getCurrentUser();
            List<User> teamMembers = new ArrayList<>();
            
            System.out.println("DEBUG: getTeamMembersByManagerId called for user: " + currentUser.getId() + " (" + currentUser.getNom() + " " + currentUser.getPrenom() + ") role: " + currentUser.getRole().name());
            
            if (currentUser.getRole().name().equals("COLLABORATEUR")) {
                // For collaborators, find their manager and other collaborators under same manager
                if (currentUser.getManager() != null) {
                    // Get the manager
                    teamMembers.add(currentUser.getManager());
                    
                    // Get other collaborators under the same manager
                    List<User> managerCollaborators = userRepository.findByManager(currentUser.getManager());
                    for (User collaborator : managerCollaborators) {
                        if (!collaborator.getId().equals(currentUser.getId())) {
                            teamMembers.add(collaborator);
                        }
                    }
                } else {
                    // Fallback: use department-based grouping
                    String department = currentUser.getDepartement();
                    if (department != null && !department.isEmpty()) {
                        // Get managers in same department
                        List<User> departmentManagers = userService.findByRole(Role.MANAGER).stream()
                            .filter(u -> department.equalsIgnoreCase(u.getDepartement()))
                            .collect(Collectors.toList());
                        teamMembers.addAll(departmentManagers);
                        
                        // Get other collaborators in same department
                        List<User> departmentCollaborators = userService.findByRole(Role.COLLABORATEUR).stream()
                            .filter(u -> department.equalsIgnoreCase(u.getDepartement()) && !u.getId().equals(currentUser.getId()))
                            .collect(Collectors.toList());
                        teamMembers.addAll(departmentCollaborators);
                    }
                }
            } else if (currentUser.getRole().name().equals("MANAGER")) {
                // For managers, return their managed employees
                User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                        .orElse(currentUser);
                
                if (managerWithEmployees.getManagedEmployees() != null) {
                    teamMembers.addAll(managerWithEmployees.getManagedEmployees());
                } else {
                    // Fallback: try direct query
                    teamMembers.addAll(userRepository.findByManagerId(currentUser.getId()));
                }
            }
            
            System.out.println("DEBUG: Returning " + teamMembers.size() + " team members");
            return ResponseEntity.ok(teamMembers);
            
        } catch (Exception e) {
            System.err.println("Error in getTeamMembersByManagerId: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> getUserById(@PathVariable @NonNull Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");
        }
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> updateUserRole(@PathVariable @NonNull Long id, @RequestParam String role) {
        try {
            Role newRole = Role.valueOf(role.toUpperCase());
            userService.updateUserRole(id, newRole, "ADMIN");
            return ResponseEntity.ok("Rôle de l'utilisateur mis à jour avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Rôle invalide: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du rôle: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> activateUser(@PathVariable @NonNull Long id) {
        try {
            userService.activateUser(id);
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"message\": \"Utilisateur activé avec succès\", \"userId\": " + id + ", \"statut\": \"actif\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"NOT_FOUND\", \"message\": \"" + e.getMessage() + "\", \"code\": 404}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Erreur lors de l'activation: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> deactivateUser(@PathVariable @NonNull Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"message\": \"Utilisateur désactivé avec succès\", \"userId\": " + id + ", \"statut\": \"inactif\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"NOT_FOUND\", \"message\": \"" + e.getMessage() + "\", \"code\": 404}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Erreur lors de la désactivation: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> deleteUser(@PathVariable @NonNull Long id) {
        try {
            userService.deleteUser(id, "ADMIN");
            return ResponseEntity.ok("Utilisateur supprimé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        User currentUser = getCurrentUser();
        List<User> users;
        
        if (currentUser.getRole().name().equals("ADMINISTRATEUR")) {
            // Administrators see all users of the requested role
            users = userService.findByRole(role);
        } else if (currentUser.getRole().name().equals("MANAGER")) {
            // Managers see only their managed employees for the requested role
            if (role == Role.COLLABORATEUR) {
                System.out.println("DEBUG: getUsersByRole called for COLLABORATEUR - filtering for manager: " + currentUser.getId());
                User managerWithEmployees = userRepository.findByIdWithManagedEmployees(currentUser.getId())
                        .orElse(currentUser);
                
                List<User> managedEmployees = managerWithEmployees.getManagedEmployees();
                System.out.println("DEBUG: Managed employees from relationship: " + (managedEmployees != null ? managedEmployees.size() : 0));
                
                if (managedEmployees == null || managedEmployees.isEmpty()) {
                    // Fallback: try to find employees by manager_id directly
                    managedEmployees = userRepository.findByManagerId(currentUser.getId());
                    System.out.println("DEBUG: Managed employees from direct query: " + managedEmployees.size());
                }
                
                // Make managedEmployees effectively final for lambda expression
                final List<User> finalManagedEmployees = managedEmployees;
                
                // Filter to show only collaborators who are managed by this manager
                List<User> allCollaborators = userService.findByRole(Role.COLLABORATEUR);
                users = allCollaborators.stream()
                        .filter(user -> finalManagedEmployees.stream()
                                .anyMatch(managed -> managed.getId().equals(user.getId())))
                        .collect(Collectors.toList());
                
                System.out.println("DEBUG: Filtered collaborators for role endpoint: " + users.size());
                for (User user : users) {
                    System.out.println("DEBUG: Final filtered user for role endpoint: " + user.getId() + " (" + user.getNom() + " " + user.getPrenom() + ")");
                }
            } else {
                // For non-collaborator roles, return empty list for managers
                users = Collections.emptyList();
            }
        } else {
            users = Collections.emptyList();
        }
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<User>> getUsersByStatus(@PathVariable Boolean statut) {
        List<User> users = userService.findByStatut(statut);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/check-status/{email}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<?> checkUserStatus(@PathVariable @NonNull String email) {
        try {
            boolean isActive = userService.isUserActive(email);
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"email\": \"" + email + "\", \"active\": " + isActive + ", \"status\": \"" + (isActive ? "actif" : "inactif") + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Erreur lors de la vérification: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUserEndpoint() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email;
            
            if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
                email = authentication.getName();
            } else {
                // Use default user email when no authentication context
                email = "collab@gmail.com";
            }
            
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"USER_NOT_FOUND\", \"message\": \"User not found\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Error getting current user: " + e.getMessage() + "\"}");
        }
    }
    
    }

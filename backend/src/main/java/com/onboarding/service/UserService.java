package com.onboarding.service;

import com.onboarding.dto.UserCreationDTO;
import com.onboarding.entity.AuditLog;
import com.onboarding.entity.Role;
import com.onboarding.entity.User;
import com.onboarding.repository.AuditLogRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordValidationService passwordValidationService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
    
    public User createUser(UserCreationDTO userDTO, String createdBy) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        
        // Créer l'utilisateur
        User user = new User();
        user.setPrenom(userDTO.getPrenom());
        user.setNom(userDTO.getNom());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // Encoder le mot de passe
        user.setRole(userDTO.getRole());
        user.setPoste(userDTO.getPoste());
        user.setDepartement(userDTO.getDepartement());
        // Set manager if provided
        if (userDTO.getManagerId() != null) {
            User manager = userRepository.findById(userDTO.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Manager non trouvé avec l'ID: " + userDTO.getManagerId()));
            user.setManager(manager);
        }
        user.setDateEmbauche(userDTO.getDateEmbauche());
        user.setTypeContrat(userDTO.getTypeContrat());
        user.setAdresse(userDTO.getAdresse());
        user.setTelephone(userDTO.getTelephone());
        user.setCin(userDTO.getCin());
        user.setDiplome(userDTO.getDiplome());
        user.setStatut(true); // Actif par défaut
        
        // Générer le token de réinitialisation
        String resetToken = emailService.generateResetToken();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(emailService.calculateTokenExpiry());
        
        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        
        // Envoyer l'email de bienvenue avec le lien de définition du mot de passe
        try {
            emailService.sendWelcomeEmail(userDTO.getEmail(), userDTO.getPrenom(), resetToken);
        } catch (Exception e) {
            // Log the error but don't fail the user creation for testing
            System.err.println("Email sending failed: " + e.getMessage());
        }
        
        try {
            notificationService.sendUserCreationNotification(savedUser);
        } catch (Exception e) {
            System.err.println("Failed to send admin user creation notification: " + e.getMessage());
        }

        if (savedUser.getManager() != null) {
            try {
                notificationService.sendUserCreationNotificationToManager(savedUser);
                System.out.println("Notification sent to manager for user creation: " + savedUser.getEmail());
            } catch (Exception e) {
                // Log the error but don't fail the user creation
                System.err.println("Failed to send notification to manager: " + e.getMessage());
            }
        }
        
        // Créer l'audit log
        AuditLog auditLog = new AuditLog(savedUser, "CRÉATION_UTILISATEUR", null, 
                "Utilisateur créé: " + userDTO.getPrenom() + " " + userDTO.getNom(), createdBy);
        auditLogRepository.save(auditLog);
        
        return savedUser;
    }
    
    /**
     * Migrate existing manager string data to foreign key relationships
     * This method should be called once after the database schema is updated
     */
    public void migrateManagerRelationships() {
        List<User> usersWithManagerName = userRepository.findAll().stream()
                .filter(user -> user.getManagerName() != null && !user.getManagerName().trim().isEmpty())
                .collect(Collectors.toList());
        
        for (User user : usersWithManagerName) {
            try {
                // Try to find a manager by name (format: "FirstName LastName")
                String managerName = user.getManagerName().trim();
                List<User> potentialManagers = userRepository.findByRole(Role.MANAGER);
                potentialManagers.addAll(userRepository.findByRole(Role.ADMINISTRATEUR));
                
                User foundManager = potentialManagers.stream()
                        .filter(manager -> (manager.getPrenom() + " " + manager.getNom()).equalsIgnoreCase(managerName))
                        .findFirst()
                        .orElse(null);
                
                if (foundManager != null) {
                    user.setManager(foundManager);
                    userRepository.save(user);
                    System.out.println("Migrated manager relationship for user: " + user.getEmail() + " -> " + foundManager.getEmail());
                } else {
                    System.out.println("Could not find manager for user: " + user.getEmail() + " with manager name: " + managerName);
                }
            } catch (Exception e) {
                System.err.println("Error migrating manager for user " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }
    
    public User updateUserRole(@NonNull Long userId, Role newRole, String modifiedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        
        Role oldRole = user.getRole();
        user.setRole(newRole);
        
        User updatedUser = userRepository.save(user);

        try {
            notificationService.sendUserUpdateNotification(updatedUser);
        } catch (Exception e) {
            System.err.println("Failed to send user update notification: " + e.getMessage());
        }
        
        AuditLog auditLog = new AuditLog(user, "MODIFICATION_ROLE", oldRole.toString(), 
                newRole.toString(), modifiedBy);
        auditLogRepository.save(auditLog);
        
        return updatedUser;
    }
    
    public User activateUser(@NonNull Long userId, String modifiedBy) {
        return changeUserStatus(userId, true, "ACTIVATION_COMPTE", modifiedBy);
    }
    
    public User deactivateUser(@NonNull Long userId, String modifiedBy) {
        return changeUserStatus(userId, false, "DÉSACTIVATION_COMPTE", modifiedBy);
    }
    
    private User changeUserStatus(@NonNull Long userId, boolean newStatus, String action, String modifiedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        
        Boolean oldStatus = user.getStatut();
        user.setStatut(newStatus);
        
        User updatedUser = userRepository.save(user);
        
        // Créer l'audit log
        AuditLog auditLog = new AuditLog(user, action, String.valueOf(oldStatus), 
                String.valueOf(newStatus), modifiedBy);
        auditLogRepository.save(auditLog);
        
        return updatedUser;
    }
    
    public User setPassword(String resetToken, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetTokenAndExpiryAfter(resetToken, LocalDateTime.now());
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Token invalide ou expiré");
        }
        
        User user = userOpt.get();
        
        // Valider le mot de passe
        passwordValidationService.validatePassword(newPassword);
        
        // Encoder et définir le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public Optional<User> findById(@NonNull Long id) {
        return userRepository.findById(id);
    }
    
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    public List<User> findByStatut(Boolean statut) {
        return userRepository.findByStatut(statut);
    }
    
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }
    
    public User save(@NonNull User user) {
        return userRepository.save(user);
    }
    
    public User activateUser(@NonNull Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatut(true);
            user.setDateModification(java.time.LocalDateTime.now());
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("Utilisateur non trouvé");
    }
    
    public User deactivateUser(@NonNull Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStatut(false);
            user.setDateModification(java.time.LocalDateTime.now());
            return userRepository.save(user);
        }
        throw new IllegalArgumentException("Utilisateur non trouvé");
    }
    
    public boolean isUserActive(@NonNull String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.map(User::getStatut).orElse(false);
    }
    
    public void deleteUser(@NonNull Long userId, String deletedBy) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        try {
            notificationService.sendUserDeletionNotification(user);
        } catch (Exception e) {
            System.err.println("Failed to send user deletion notification: " + e.getMessage());
        }
        
        userRepository.delete(Objects.requireNonNull(user));
        
        // Créer l'audit log
        AuditLog auditLog = new AuditLog(user, "SUPPRESSION_UTILISATEUR", 
                "Utilisateur: " + user.getPrenom() + " " + user.getNom(), null, deletedBy);
        auditLogRepository.save(auditLog);
    }
}

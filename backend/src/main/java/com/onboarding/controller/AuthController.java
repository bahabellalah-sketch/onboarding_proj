package com.onboarding.controller;

import com.onboarding.dto.AuthRequestDTO;
import com.onboarding.dto.AuthResponseDTO;
import com.onboarding.dto.PasswordResetDTO;
import com.onboarding.entity.User;
import com.onboarding.service.EmailService;
import com.onboarding.service.JwtService;
import com.onboarding.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://192.168.*:*", "http://10.*:*"})
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    // Endpoint public pour créer le premier utilisateur admin (sans authentification)
    @PostMapping("/init-admin")
    @CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "http://192.168.*:*", "http://10.*:*"})
    public ResponseEntity<?> initializeAdmin() {
        try {
            // Vérifier si l'utilisateur admin existe déjà
            if (userService.findByEmail("admin@test.com").isPresent()) {
                return ResponseEntity.ok("Utilisateur admin existe déjà");
            }
            
            // Créer l'utilisateur admin avec mot de passe encodé
            User admin = new User();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setRole(com.onboarding.entity.Role.ADMINISTRATEUR);
            admin.setPoste("Administrateur Système");
            admin.setDepartement("IT");
            admin.setStatut(true);
            admin.setDateCreation(java.time.LocalDateTime.now());
            
            userService.save(admin);
            
            return ResponseEntity.ok("{\"message\": \"Utilisateur admin créé avec succès\", \"email\": \"admin@test.com\", \"password\": \"Admin123!\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erreur lors de la création: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequestDTO loginRequest) {
        try {
            Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou mot de passe incorrect");
            }
            
            User user = userOpt.get();
            
            // Vérifier si le compte est actif
            if (!user.getStatut()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Votre compte est désactivé. Veuillez contacter l'administrateur.");
            }
            
            // Vérifier si l'utilisateur a un mot de passe (première connexion)
            // TEMPORAIREMENT DÉSACTIVÉ POUR LES TESTS
            // if (user.getPassword() == null || user.getPassword().isEmpty()) {
            //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            //             .body("Veuillez d'abord définir votre mot de passe via le lien envoyé par email.");
            // }
            
            // Vérifier le mot de passe
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou mot de passe incorrect");
            }
            
            // Générer le token JWT avec toutes les informations utilisateur
            String token = jwtService.generateToken(
                user.getEmail(), 
                user.getRole().toString(),
                user.getPrenom(),
                user.getNom(),
                user.getId()
            );
            
            // Créer la réponse
            AuthResponseDTO response = new AuthResponseDTO(
                    token, 
                    user.getEmail(), 
                    user.getRole().toString(),
                    user.getPrenom(),
                    user.getNom()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'authentification: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        try {
            // Vérifier si l'email existe déjà
            if (userService.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"EMAIL_EXISTS\", \"message\": \"Cet email est déjà utilisé\", \"code\": 400}");
            }
            
            // Encoder le mot de passe
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            
            // Définir le statut par défaut
            user.setStatut(true);
            
            // Sauvegarder l'utilisateur
            User savedUser = userService.save(user);
            
            // Envoyer l'email de configuration du mot de passe (temporairement désactivé pour les tests)
            // emailService.sendPasswordSetupEmail(savedUser.getEmail(), savedUser.getResetToken());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"success\": true, \"message\": \"Utilisateur créé avec succès\", \"userId\": " + savedUser.getId() + "}");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_ERROR\", \"message\": \"Erreur lors de la création: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Aucun utilisateur trouvé avec cet email");
            }
            
            User user = userOpt.get();
            
            // Générer le token de réinitialisation
            String resetToken = emailService.generateResetToken();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(emailService.calculateTokenExpiry());
            
            // Sauvegarder l'utilisateur
            userService.save(user);
            
            // Envoyer l'email de réinitialisation
            emailService.sendPasswordResetEmail(email, resetToken);
            
            return ResponseEntity.ok("Un email de réinitialisation a été envoyé à votre adresse email");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'envoi de l'email: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        try {
            userService.setPassword(passwordResetDTO.getToken(), passwordResetDTO.getPassword());
            
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la réinitialisation du mot de passe: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Dans une implémentation JWT, le logout est géré côté client
        // en supprimant simplement le token
        return ResponseEntity.ok("Déconnexion réussie");
    }
}

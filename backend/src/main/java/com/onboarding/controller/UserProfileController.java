package com.onboarding.controller;

import com.onboarding.entity.User;
import com.onboarding.entity.UserProfile;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@CrossOrigin(origins = "http://localhost:3000")
@SuppressWarnings("nullness")
public class UserProfileController {
    
    @Autowired
    private UserProfileService userProfileService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/create")
    public ResponseEntity<?> createProfile(@RequestParam(required = false) Long userId, @RequestParam String email) {
        try {
            UserProfile createdProfile = userProfileService.createUserProfile(null, email);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"success\": true, \"message\": \"Profil créé avec succès. ID: " + createdProfile.getId() + "\", \"profileId\": " + createdProfile.getId() + "}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"BAD_REQUEST\", \"message\": \"" + e.getMessage() + "\", \"code\": 400}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la création du profil: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @PostMapping("/create-with-user")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> createProfileWithUser(@NonNull @RequestParam Long userId, @RequestParam String email) {
        try {
            // Récupérer l'utilisateur
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
            
            UserProfile profile = userProfileService.createUserProfile(user, email);
            Long profileId = profile.getId();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("{\"success\": true, \"message\": \"Profil créé avec succès. ID: " + profileId + "\", \"profileId\": " + profileId + "}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\": \"BAD_REQUEST\", \"message\": \"" + e.getMessage() + "\", \"code\": 400}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la création du profil: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @GetMapping("/check/{email}")
    public ResponseEntity<?> checkEmail(@PathVariable String email) {
        try {
            UserProfile userProfile = userProfileService.findByEmail(email);
            if (userProfile != null) {
                return ResponseEntity.ok()
                        .body("{\"success\": true, \"message\": \"Profil trouvé\", \"profileId\": " + userProfile.getId() + "}");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"NOT_FOUND\", \"message\": \"Aucun profil trouvé pour cet email\", \"code\": 404}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la vérification: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<?> listProfiles() {
        try {
            List<UserProfile> profiles = userProfileService.findAll();
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors du chargement: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> deleteProfile(@PathVariable @NonNull Long id) {
        try {
            userProfileService.deleteProfile(id);
            return ResponseEntity.ok()
                    .body("{\"success\": true, \"message\": \"Profil supprimé avec succès\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"INTERNAL_SERVER_ERROR\", \"message\": \"Erreur lors de la suppression: " + e.getMessage() + "\", \"code\": 500}");
        }
    }
    
    // DTO pour la réponse
    public static class EmailVerificationResponse {
        private boolean verified;
        
        public EmailVerificationResponse(boolean verified) {
            this.verified = verified;
        }
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
    }
}

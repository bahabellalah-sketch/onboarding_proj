package com.onboarding.controller;

import com.onboarding.entity.EmailVerification;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/email-verification")
// @CrossOrigin is not needed — global CORS config in SecurityConfig handles all origins
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/current-user-status")
    public ResponseEntity<?> getCurrentUserVerificationStatus() {
        try {
            // Get current authenticated user from JWT token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            // Extract user ID from authentication
            String email = authentication.getName();
            System.out.println("Checking email verification status for authenticated user: " + email);

            // Get user ID from email (this assumes the email is the username in JWT)
            // You may need to adjust this based on your JWT token structure
            Long userId = getUserIdFromEmail(email);
            
            if (userId == null) {
                return ResponseEntity.status(400).body("User not found");
            }

            // Check verification status from database
            boolean isVerified = emailVerificationService.isEmailVerified(userId);
            Optional<EmailVerification> verification = emailVerificationService.getLatestVerification(userId);
            
            Map<String, Object> response = Map.of(
                "verified", isVerified,
                "hasPendingVerification", verification.isPresent() && !verification.get().getIsVerified(),
                "isExpired", verification.isPresent() && verification.get().isExpired()
            );
            
            System.out.println("Email verification status for current user: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error checking current user email verification status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error checking verification status: " + e.getMessage());
        }
    }

    // Helper method to get user ID from email
    private Long getUserIdFromEmail(String email) {
        try {
            Optional<User> user = userRepository.findByEmail(email);
            return user.map(User::getId).orElse(null);
        } catch (Exception e) {
            System.err.println("Error getting user ID from email: " + e.getMessage());
            return null;
        }
    }

    @PostMapping("/send/{userId}")
    public ResponseEntity<?> sendVerificationEmail(@NonNull @PathVariable Long userId) {
        try {
            String token = emailVerificationService.sendVerificationEmail(userId);
            return ResponseEntity.ok(Map.of(
                "message", "Verification email sent successfully",
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending verification email: " + e.getMessage());
        }
    }

    
    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getVerificationStatus(@NonNull @PathVariable Long userId) {
        try {
            System.out.println("Checking email verification status for user ID: " + userId);
            boolean isVerified = emailVerificationService.isEmailVerified(userId);
            Optional<EmailVerification> verification = emailVerificationService.getLatestVerification(userId);
            
            Map<String, Object> response = Map.of(
                "isVerified", isVerified,
                "hasPendingVerification", verification.isPresent() && !verification.get().getIsVerified(),
                "isExpired", verification.isPresent() && verification.get().isExpired()
            );
            
            System.out.println("Email verification status response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error checking email verification status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error checking verification status: " + e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            System.out.println("Email verification request received with token: " + token);
            boolean success = emailVerificationService.verifyEmail(token);
            System.out.println("Email verification result: " + success);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully",
                    "success", true
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid or expired verification token",
                    "success", false
                ));
            }
        } catch (Exception e) {
            System.err.println("Error verifying email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error verifying email: " + e.getMessage());
        }
    }

    @PostMapping("/resend/{userId}")
    public ResponseEntity<?> resendVerificationEmail(@NonNull @PathVariable Long userId) {
        try {
            String token = emailVerificationService.sendVerificationEmail(userId);
            return ResponseEntity.ok(Map.of(
                "message", "Verification email resent successfully",
                "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resending verification email: " + e.getMessage());
        }
    }
}

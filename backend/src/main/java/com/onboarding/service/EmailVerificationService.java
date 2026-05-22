package com.onboarding.service;

import com.onboarding.entity.EmailVerification;
import com.onboarding.entity.User;
import com.onboarding.repository.EmailVerificationRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;

    @Transactional
    public String sendVerificationEmail(@NonNull Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        
        // Delete any existing verification tokens for this user
        emailVerificationRepository.deleteByUser(user);
        
        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerification emailVerification = new EmailVerification(user, verificationToken);
        
        // Save the verification token
        emailVerificationRepository.save(emailVerification);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        
        return verificationToken;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        System.out.println("EmailVerificationService: Verifying token: " + token);
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByVerificationToken(token);
        if (verificationOpt.isEmpty()) {
            System.out.println("EmailVerificationService: Token not found in database");
            return false;
        }

        EmailVerification verification = verificationOpt.get();
        User user = verification.getUser();
        
        // Check if user exists
        if (user == null) {
            System.out.println("EmailVerificationService: User not found for this token");
            return false;
        }
        
        System.out.println("EmailVerificationService: Found verification record for user: " + user.getEmail());
        
        // Check if token is expired
        if (verification.isExpired()) {
            System.out.println("EmailVerificationService: Token expired. Created: " + verification.getCreatedAt() + ", Expires: " + verification.getExpiresAt());
            return false;
        }
        
        // Check if already verified
        if (verification.getIsVerified()) {
            System.out.println("EmailVerificationService: Token already verified");
            return true; // Already verified
        }
        
        // Mark as verified
        System.out.println("EmailVerificationService: Marking token as verified");
        verification.markAsVerified();
        emailVerificationRepository.save(verification);
        
        // Update user's email verification status
        user.setEmailVerified(true);
        userRepository.save(user);
        
        return true;
    }

    public boolean isEmailVerified(@NonNull Long userId) {
        System.out.println("Checking email verification for user ID: " + userId);
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("User not found with ID: " + userId);
            return false;
        }
        
        User user = userOpt.get();
        Boolean emailVerified = user.getEmailVerified();
        
        // Fix null emailVerified values
        if (emailVerified == null) {
            System.out.println("Fixing null emailVerified for user: " + user.getEmail());
            user.setEmailVerified(false);
            userRepository.save(user);
            emailVerified = false;
        }
        
        System.out.println("User found: " + user.getEmail() + ", emailVerified: " + emailVerified);
        return emailVerified;
    }

    @Transactional
    public void cleanupExpiredTokens() {
        emailVerificationRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    public Optional<EmailVerification> getLatestVerification(@NonNull Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        return emailVerificationRepository.findByUser(userOpt.get());
    }
}

package com.onboarding.controller;

import com.onboarding.dto.ChangePasswordDTO;
import com.onboarding.dto.UpdateProfileDTO;
import com.onboarding.dto.UserProfileDTO;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                return userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            }
        }
        throw new RuntimeException("User not authenticated");
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile(@NonNull @PathVariable Long userId) {
        try {
            UserProfileDTO user = profileService.getUserProfile(getCurrentUser(), userId);
            return ResponseEntity.ok(user);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@NonNull @PathVariable Long userId, @RequestBody UpdateProfileDTO profileDTO) {
        try {
            return ResponseEntity.ok(profileService.updateProfile(getCurrentUser(), userId, profileDTO));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@NonNull @PathVariable Long userId, @RequestBody ChangePasswordDTO passwordDTO) {
        try {
            boolean success = profileService.changePassword(getCurrentUser(), userId, passwordDTO);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{userId}/upload-photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadProfilePhoto(@NonNull @PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please select a file to upload"));
            }
            String photoUrl = profileService.uploadProfilePhoto(getCurrentUser(), userId, file);
            return ResponseEntity.ok(photoUrl);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

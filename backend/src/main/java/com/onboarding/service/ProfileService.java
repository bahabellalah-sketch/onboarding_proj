package com.onboarding.service;

import com.onboarding.dto.ChangePasswordDTO;
import com.onboarding.dto.UpdateProfileDTO;
import com.onboarding.dto.UserProfileDTO;
import com.onboarding.entity.Role;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String UPLOAD_DIR = "uploads/profile-photos/";

    public boolean canViewProfile(@NonNull User viewer, @NonNull Long targetUserId) {
        if (viewer.getId().equals(targetUserId)) {
            return true;
        }
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) {
            return false;
        }
        if (viewer.getRole() == Role.ADMINISTRATEUR) {
            return true;
        }
        if (viewer.getRole() == Role.MANAGER) {
            User targetWithManager = userRepository.findByIdWithManager(targetUserId).orElse(target);
            if (targetWithManager.getManager() != null
                    && viewer.getId().equals(targetWithManager.getManager().getId())) {
                return true;
            }
            return userRepository.findByManagerId(viewer.getId()).stream()
                    .anyMatch(u -> u.getId().equals(targetUserId));
        }
        if (viewer.getRole() == Role.COLLABORATEUR) {
            return isSameTeam(viewer, target);
        }
        return false;
    }

    public boolean canEditProfile(@NonNull User viewer, @NonNull Long targetUserId) {
        return viewer.getId().equals(targetUserId) || viewer.getRole() == Role.ADMINISTRATEUR;
    }

    private boolean isSameTeam(User viewer, User target) {
        if (viewer.getDepartement() != null && viewer.getDepartement().equalsIgnoreCase(target.getDepartement())) {
            return true;
        }
        User viewerWithManager = userRepository.findByIdWithManager(viewer.getId()).orElse(viewer);
        User targetWithManager = userRepository.findByIdWithManager(target.getId()).orElse(target);
        if (viewerWithManager.getManager() != null && targetWithManager.getManager() != null) {
            return viewerWithManager.getManager().getId().equals(targetWithManager.getManager().getId());
        }
        return false;
    }

    @NonNull
    public User updateProfile(@NonNull User viewer, @NonNull Long userId, UpdateProfileDTO profileDTO) {
        if (!canEditProfile(viewer, userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier ce profil");
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();
        
        if (profileDTO.getPrenom() != null) {
            user.setPrenom(profileDTO.getPrenom());
        }
        if (profileDTO.getNom() != null) {
            user.setNom(profileDTO.getNom());
        }
        if (profileDTO.getEmail() != null) {
            user.setEmail(profileDTO.getEmail());
        }
        if (profileDTO.getDateNaissance() != null) {
            user.setDateNaissance(profileDTO.getDateNaissance());
        }
        if (profileDTO.getTelephone() != null) {
            user.setTelephone(profileDTO.getTelephone());
        }
        if (profileDTO.getAdresse() != null) {
            user.setAdresse(profileDTO.getAdresse());
        }
        if (profileDTO.getPoste() != null) {
            user.setPoste(profileDTO.getPoste());
        }
        if (profileDTO.getDepartement() != null) {
            user.setDepartement(profileDTO.getDepartement());
        }
        if (profileDTO.getCin() != null) {
            user.setCin(profileDTO.getCin());
        }
        if (profileDTO.getDiplome() != null) {
            user.setDiplome(profileDTO.getDiplome());
        }

        return userRepository.save(user);
    }

    public boolean changePassword(@NonNull User viewer, @NonNull Long userId, ChangePasswordDTO passwordDTO) {
        if (!canEditProfile(viewer, userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier ce mot de passe");
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            return false;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    @NonNull
    public String uploadProfilePhoto(@NonNull User viewer, @NonNull Long userId, MultipartFile file) throws IOException {
        if (!canEditProfile(viewer, userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette photo");
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Filename cannot be null");
        }
        
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "user_" + userId + "_profile" + fileExtension;
        
        Path filePath = uploadPath.resolve(newFilename);
        
        // Replace existing file if it exists
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Update user profile photo URL - use forward slashes for web URLs
        User user = userOpt.get();
        String photoUrl = "/uploads/profile-photos/" + newFilename;
        user.setProfilePhotoUrl(photoUrl);
        userRepository.save(user);

        return user.getProfilePhotoUrl();
    }

    @NonNull
    public UserProfileDTO getUserProfile(@NonNull User viewer, @NonNull Long userId) {
        if (!canViewProfile(viewer, userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à consulter ce profil");
        }
        User user = userRepository.findByIdWithManager(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setPrenom(user.getPrenom());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().toString() : null);
        dto.setTelephone(user.getTelephone());
        dto.setAdresse(user.getAdresse());
        dto.setPoste(user.getPoste());
        dto.setDepartement(user.getDepartement());
        dto.setCin(user.getCin());
        dto.setDiplome(user.getDiplome());
        dto.setDateNaissance(user.getDateNaissance());
        dto.setProfilePhotoUrl(user.getProfilePhotoUrl());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setStatut(user.getStatut());
        dto.setDateCreation(user.getDateCreation());
        dto.setDateEmbauche(user.getDateEmbauche());
        if (user.getManager() != null) {
            dto.setManagerName(user.getManager().getPrenom() + " " + user.getManager().getNom());
        } else {
            dto.setManagerName(user.getManagerName());
        }
        
        return dto;
    }
}

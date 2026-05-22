package com.onboarding.service;

import com.onboarding.entity.User;
import com.onboarding.entity.UserProfile;
import com.onboarding.repository.UserProfileRepository;
import com.onboarding.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@SuppressWarnings("unused")
public class UserProfileService {
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private EmailService emailService;
    
    // Constructor pour s'assurer que EmailService est bien utilisé
    public UserProfileService() {
        // Ce constructeur garantit que toutes les dépendances sont correctement injectées
    }
    
    public UserProfile createUserProfile(User user, String email) {
        // Vérifier si le profil existe déjà
        if (userProfileRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Un profil avec cet email existe déjà");
        }
        
        UserProfile userProfile = new UserProfile();
        userProfile.setEmail(email);
        
        // Si un utilisateur est fourni, l'associer
        if (user != null) {
            userProfile.setUser(user);
        }
        
        return userProfileRepository.save(userProfile);
    }
    
    public UserProfile findByEmail(String email) {
        return userProfileRepository.findByEmail(email).orElse(null);
    }
    
    public Optional<UserProfile> findByEmailOptional(String email) {
        return userProfileRepository.findByEmail(email);
    }
    
    public UserProfile findByUser(User user) {
        return userProfileRepository.findByUser(user).orElse(null);
    }
    
    public UserProfile updateUserProfile(User user, String newEmail) {
        Optional<UserProfile> userProfileOpt = findByEmailOptional(newEmail);
        
        if (userProfileOpt.isPresent()) {
            UserProfile userProfile = userProfileOpt.get();
            
            // Si l'email change, mettre à jour
            if (!userProfile.getEmail().equals(newEmail)) {
                if (userProfileRepository.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("Cet email est déjà utilisé");
                }
                
                userProfile.setEmail(newEmail);
            }
            
            return userProfileRepository.save(userProfile);
        } else {
            // Créer un nouveau profil
            return createUserProfile(user, newEmail);
        }
    }
    
    public List<UserProfile> findAll() {
        return userProfileRepository.findAll();
    }
    
    public void deleteProfile(@NonNull Long id) {
        userProfileRepository.deleteById(id);
    }
    
    public boolean existsByEmail(String email) {
        return userProfileRepository.existsByEmail(email);
    }
    
    public boolean existsByEmailOptional(String email) {
        return userProfileRepository.existsByEmail(email);
    }
}

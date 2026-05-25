package com.onboarding.config;

import com.onboarding.entity.Role;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Après un vidage de la base, recrée automatiquement un compte administrateur par défaut.
 */
@Component
public class AdminBootstrap {

    public static final String DEFAULT_ADMIN_EMAIL = "admin@test.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureDefaultAdmin() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setNom("Admin");
        admin.setPrenom("System");
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setPoste("Administrateur Système");
        admin.setDepartement("IT");
        admin.setStatut(true);
        admin.setEmailVerified(true);
        admin.setDateCreation(LocalDateTime.now());
        userRepository.save(admin);
        System.out.println("AdminBootstrap: compte admin créé — " + DEFAULT_ADMIN_EMAIL + " / " + DEFAULT_ADMIN_PASSWORD);
    }
}

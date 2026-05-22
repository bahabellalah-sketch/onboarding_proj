package com.onboarding.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class PasswordValidationService {
    
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final int MIN_LENGTH = 8;
    
    public void validatePassword(String password) throws IllegalArgumentException {
        if (password == null) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être nul");
        }
        
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au minimum " + MIN_LENGTH + " caractères");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une majuscule");
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre");
        }
    }
    
    public boolean isValid(String password) {
        try {
            validatePassword(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

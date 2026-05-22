package com.onboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetDTO {
    
    @NotBlank(message = "Le token est obligatoire")
    private String token;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
    
    // Constructors
    public PasswordResetDTO() {}
    
    public PasswordResetDTO(String token, String password) {
        this.token = token;
        this.password = password;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

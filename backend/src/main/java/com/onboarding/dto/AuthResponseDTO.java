package com.onboarding.dto;

public class AuthResponseDTO {
    
    private String token;
    private String email;
    private String role;
    private String prenom;
    private String nom;
    
    // Constructors
    public AuthResponseDTO() {}
    
    public AuthResponseDTO(String token, String email, String role, String prenom, String nom) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.prenom = prenom;
        this.nom = nom;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}

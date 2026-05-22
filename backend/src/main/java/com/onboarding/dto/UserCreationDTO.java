package com.onboarding.dto;

import com.onboarding.entity.Role;
import com.onboarding.entity.TypeContrat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UserCreationDTO {
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
    
    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
    
    private String poste;
    
    private String departement;
    
    private Long managerId;
    
    private LocalDateTime dateEmbauche;
    
    private TypeContrat typeContrat;
    
    private String adresse;
    
    private String telephone;
    
    private String cin;
    
    private String diplome;
    
    // Constructors
    public UserCreationDTO() {}
    
    // Getters and Setters
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    
    public LocalDateTime getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDateTime dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    
    public TypeContrat getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContrat typeContrat) { this.typeContrat = typeContrat; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    
    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }
}

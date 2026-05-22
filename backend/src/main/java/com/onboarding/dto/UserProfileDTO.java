package com.onboarding.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserProfileDTO {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private String role;
    private String telephone;
    private String adresse;
    private String poste;
    private String departement;
    private String cin;
    private String diplome;
    private LocalDate dateNaissance;
    private String profilePhotoUrl;
    private Boolean emailVerified;
    private Boolean statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEmbauche;
    private String managerName;

    // Constructors
    public UserProfileDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getStatut() { return statut; }
    public void setStatut(Boolean statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDateTime dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
}

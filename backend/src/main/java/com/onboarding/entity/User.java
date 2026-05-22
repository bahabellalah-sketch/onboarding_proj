package com.onboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom", nullable = false)
    private String prenom;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom", nullable = false)
    private String nom;
    
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Column(name = "poste")
    private String poste;
    
    @Column(name = "departement")
    private String departement;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonBackReference
    private User manager;
    
    @Column(name = "manager")
    private String managerName; // Keep old field for migration
    
    @Column(name = "date_embauche")
    private LocalDateTime dateEmbauche;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat")
    private TypeContrat typeContrat;
    
    @Column(name = "adresse")
    private String adresse;
    
    @Column(name = "telephone")
    private String telephone;
    
    @Column(name = "cin")
    private String cin;
    
    @Column(name = "diplome")
    private String diplome;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "reset_token")
    private String resetToken;
    
    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    
    @Column(name = "statut", nullable = false)
    private Boolean statut = true; // Actif par défaut
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    @Column(name = "email_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean emailVerified = false;
    
    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;
    
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;
    
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> managedEmployees;
    
    // Constructors
    public User() {}
    
    public User(String prenom, String nom, String email, Role role) {
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public User getManager() { return manager; }
    public void setManager(User manager) { this.manager = manager; }
    
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    
    public List<User> getManagedEmployees() { return managedEmployees; }
    public void setManagedEmployees(List<User> managedEmployees) { this.managedEmployees = managedEmployees; }
    
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
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    
    public Boolean getStatut() { return statut; }
    public void setStatut(Boolean statut) { 
        this.statut = statut;
        this.dateModification = LocalDateTime.now();
    }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    @JsonIgnore
    public List<AuditLog> getAuditLogs() { return auditLogs; }
    public void setAuditLogs(List<AuditLog> auditLogs) { this.auditLogs = auditLogs; }
    
    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
    
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
}

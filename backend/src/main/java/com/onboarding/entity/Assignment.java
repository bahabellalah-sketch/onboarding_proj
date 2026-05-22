package com.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignments")
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id", nullable = false)
    private OnboardingParcours parcours;
    
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;
    
    @Column(name = "date_fin_previsionnelle")
    private LocalDate dateFinPrevisionnelle;
    
    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutAssignment statut;
    
    @Column(name = "pourcentage_avancement", nullable = false)
    private Integer pourcentageAvancement = 0;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;
    
    @Column(name = "date_modification")
    private LocalDate dateModification;
    
    @Column(name = "assigne_par", nullable = false)
    private String assignePar;
    
    public Assignment() {
        this.dateCreation = LocalDate.now();
        this.statut = StatutAssignment.EN_ATTENTE; // Default status, will be updated based on dates
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public OnboardingParcours getParcours() {
        return parcours;
    }
    
    public void setParcours(OnboardingParcours parcours) {
        this.parcours = parcours;
    }
    
    public LocalDate getDateDebut() {
        return dateDebut;
    }
    
    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
        // Automatically set status based on date
        updateStatusBasedOnDates();
    }
    
    public LocalDate getDateFinPrevisionnelle() {
        return dateFinPrevisionnelle;
    }
    
    public void setDateFinPrevisionnelle(LocalDate dateFinPrevisionnelle) {
        this.dateFinPrevisionnelle = dateFinPrevisionnelle;
    }
    
    public LocalDate getDateFinReelle() {
        return dateFinReelle;
    }
    
    public void setDateFinReelle(LocalDate dateFinReelle) {
        this.dateFinReelle = dateFinReelle;
    }
    
    public StatutAssignment getStatut() {
        return statut;
    }
    
    public void setStatut(StatutAssignment statut) {
        this.statut = statut;
    }
    
    public Integer getPourcentageAvancement() {
        return pourcentageAvancement;
    }
    
    public void setPourcentageAvancement(Integer pourcentageAvancement) {
        this.pourcentageAvancement = pourcentageAvancement;
    }
    
    public LocalDate getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDate getDateModification() {
        return dateModification;
    }
    
    public void setDateModification(LocalDate dateModification) {
        this.dateModification = dateModification;
    }
    
    public String getAssignePar() {
        return assignePar;
    }
    
    public void setAssignePar(String assignePar) {
        this.assignePar = assignePar;
    }
    
    public String getTitre() {
        return parcours != null ? parcours.getTitre() : "Assignment";
    }
    
    /**
     * Automatically updates the assignment status based on current date and assignment dates
     * This method handles the automatic status transitions:
     * - EN_ATTENTE: if dateDebut is in the future
     * - EN_COURS: if today is between dateDebut and dateFinPrevisionnelle (inclusive)
     * - EN_RETARD: if today is after dateFinPrevisionnelle and status is not already TERMINE, EN_PAUSE, or ANNULE
     */
    public void updateStatusBasedOnDates() {
        if (dateDebut == null) {
            return;
        }
        
        LocalDate today = LocalDate.now();
        
        // Don't override manually set admin statuses (EN_PAUSE, ANNULE, TERMINE)
        if (statut != null && (statut == StatutAssignment.EN_PAUSE || 
            statut == StatutAssignment.ANNULE || 
            statut == StatutAssignment.TERMINE)) {
            return;
        }
        
        // If start date is in the future, set to EN_ATTENTE
        if (today.isBefore(dateDebut)) {
            statut = StatutAssignment.EN_ATTENTE;
            return;
        }
        
        // If we have an end date and it's passed, set to EN_RETARD
        if (dateFinPrevisionnelle != null && today.isAfter(dateFinPrevisionnelle)) {
            statut = StatutAssignment.EN_RETARD;
            return;
        }
        
        // If we reach here, the assignment should be EN_COURS
        // (today is on or after dateDebut and not past dateFinPrevisionnelle)
        statut = StatutAssignment.EN_COURS;
    }
}

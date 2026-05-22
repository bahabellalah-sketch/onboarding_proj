package com.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @Column(name = "ancienne_valeur")
    private String ancienneValeur;
    
    @Column(name = "nouvelle_valeur")
    private String nouvelleValeur;
    
    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();
    
    @Column(name = "effectue_par")
    private String effectuePar;
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(User user, String action, String ancienneValeur, String nouvelleValeur, String effectuePar) {
        this.user = user;
        this.action = action;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.effectuePar = effectuePar;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }
    
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
    
    public String getEffectuePar() { return effectuePar; }
    public void setEffectuePar(String effectuePar) { this.effectuePar = effectuePar; }
}

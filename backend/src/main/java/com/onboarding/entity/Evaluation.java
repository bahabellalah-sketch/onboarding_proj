package com.onboarding.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
public class Evaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    @JsonIgnore
    private Checklist checklist;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Column(name = "rating", nullable = false)
    private Integer rating;
    
    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;
    
    // Constructors
    public Evaluation() {}
    
    public Evaluation(Checklist checklist, User user, Integer rating, String comment) {
        this.checklist = checklist;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.dateEvaluation = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Checklist getChecklist() {
        return checklist;
    }
    
    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }
    
    public void setDateEvaluation(LocalDateTime dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }
}

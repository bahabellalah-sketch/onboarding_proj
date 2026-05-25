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

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType = EvaluationType.ETAPE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = true)
    @JsonIgnore
    private Checklist checklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = true)
    @JsonIgnore
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "recommendation", length = 32)
    private String recommendation;

    @Column(name = "date_evaluation", nullable = false)
    private LocalDateTime dateEvaluation;

    public Evaluation() {}

    public Evaluation(Checklist checklist, User user, Integer rating, String comment) {
        this.evaluationType = EvaluationType.ETAPE;
        this.checklist = checklist;
        this.assignment = checklist != null ? checklist.getAssignment() : null;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.dateEvaluation = LocalDateTime.now();
    }

    public Evaluation(Assignment assignment, User user, EvaluationType type, Integer rating, String comment, String recommendation) {
        this.evaluationType = type;
        this.assignment = assignment;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
        this.recommendation = recommendation;
        this.dateEvaluation = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EvaluationType getEvaluationType() { return evaluationType; }
    public void setEvaluationType(EvaluationType evaluationType) { this.evaluationType = evaluationType; }

    public Checklist getChecklist() { return checklist; }
    public void setChecklist(Checklist checklist) { this.checklist = checklist; }

    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public LocalDateTime getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDateTime dateEvaluation) { this.dateEvaluation = dateEvaluation; }
}

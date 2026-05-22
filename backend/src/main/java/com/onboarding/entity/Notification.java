package com.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "message", nullable = false)
    private String message;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "sender_id", nullable = true)
    private Long senderId;
    
    @Column(name = "entity_type", nullable = true)
    private String entityType;
    
    @Column(name = "entity_id", nullable = true)
    private Long entityId;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_lecture", nullable = true)
    private LocalDateTime dateLecture;
    
    @Column(name = "is_read", nullable = false)
    private Boolean read = false;
    
    // Constructors
    public Notification() {
        this.read = false;
    }
    
    public Notification(String message, NotificationType type, Long userId) {
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.read = false;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }
    
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
}

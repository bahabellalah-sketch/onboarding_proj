package com.onboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_messages", indexes = {
        @Index(name = "idx_team_messages_team_key", columnList = "team_key"),
        @Index(name = "idx_team_messages_sent_at", columnList = "sent_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TeamMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_key", nullable = false, length = 120)
    private String teamKey;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamKey() { return teamKey; }
    public void setTeamKey(String teamKey) { this.teamKey = teamKey; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
}

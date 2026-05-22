package com.onboarding.dto;

import java.time.LocalDateTime;

public class TeamMessageDTO {
    private Long id;
    private String teamKey;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private boolean canModify;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamKey() { return teamKey; }
    public void setTeamKey(String teamKey) { this.teamKey = teamKey; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }

    public boolean isCanModify() { return canModify; }
    public void setCanModify(boolean canModify) { this.canModify = canModify; }
}

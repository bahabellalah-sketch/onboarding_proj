package com.onboarding.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendTeamMessageRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 4000, message = "Message trop long (max 4000 caractères)")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

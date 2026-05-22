package com.onboarding.dto;

/**
 * Request DTO for AI-powered full onboarding program generation.
 * The user simply provides a text description of what they need.
 */
public class AiParcoursRequest {

    private String prompt;

    public AiParcoursRequest() {}

    public AiParcoursRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
}
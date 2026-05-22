package com.onboarding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Client for calling an AI LLM API (OpenAI-compatible).
 * Works with OpenAI, Ollama, or any OpenAI-compatible endpoint.
 */
@Service
public class AiClient {

    @Value("${ai.api.url:http://localhost:11434/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model:mistral}")
    private String model;

    @Value("${ai.temperature:0.8}")
    private double temperature;

    @Autowired
    @Qualifier("aiRestTemplate")
    private RestTemplate restTemplate;

    /**
     * Call the AI with a system message and a user message.
     * Returns the text content of the assistant's response.
     */
    public String chat(String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (apiKey != null && !apiKey.isBlank()) {
            headers.setBearerAuth(apiKey);
        }

        var messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );

        var requestBody = Map.of(
                "model", model,
                "messages", messages,
                "temperature", temperature,
                "max_tokens", 4096,
                "stream", false
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println("AI Client: Calling " + apiUrl + " with model: " + model);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            if (response == null) {
                throw new RuntimeException("AI API returned null response");
            }

            System.out.println("AI Client: Response received, keys: " + response.keySet());

            // Handle potential error response
            if (response.containsKey("error")) {
                throw new RuntimeException("AI API error: " + response.get("error"));
            }

            // Parse the response to extract the message content
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("AI API returned no choices");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("AI API returned no message in first choice");
            }

            String content = (String) message.get("content");
            if (content == null || content.isBlank()) {
                throw new RuntimeException("AI API returned empty content");
            }

            return content.trim();
        } catch (Exception e) {
            throw new RuntimeException("AI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Simple health check to see if the AI endpoint is reachable.
     */
    public boolean isAvailable() {
        try {
            chat("Respond with just 'OK'.", "ping");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
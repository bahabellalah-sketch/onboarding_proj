package com.onboarding.controller;

import com.onboarding.dto.SendTeamMessageRequest;
import com.onboarding.dto.TeamMessageDTO;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.TeamChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team-chat")
@CrossOrigin(
        origins = "http://localhost:3000",
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class TeamChatController {

    @Autowired
    private TeamChatService teamChatService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/messages")
    public ResponseEntity<?> getMessages() {
        User current = getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }
        List<TeamMessageDTO> messages = teamChatService.getMessagesForUser(current);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody SendTeamMessageRequest request) {
        User current = getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }
        try {
            TeamMessageDTO saved = teamChatService.sendMessage(current, request.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<?> updateMessage(@PathVariable Long id, @Valid @RequestBody SendTeamMessageRequest request) {
        User current = getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }
        try {
            TeamMessageDTO updated = teamChatService.updateMessage(current, id, request.getContent());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        User current = getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
        }
        try {
            teamChatService.deleteMessage(current, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        if (principal instanceof org.springframework.security.core.userdetails.User) {
            String email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return userRepository.findByEmail(email).orElse(null);
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}

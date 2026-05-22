package com.onboarding.service;

import com.onboarding.dto.TeamMessageDTO;
import com.onboarding.entity.Role;
import com.onboarding.entity.TeamMessage;
import com.onboarding.entity.User;
import com.onboarding.repository.TeamMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TeamChatService {

    public static final int MODIFY_WINDOW_MINUTES = 15;

    @Autowired
    private TeamMessageRepository teamMessageRepository;

    public String resolveTeamKey(User user) {
        if (user.getRole() == Role.MANAGER) {
            return "manager-" + user.getId();
        }
        if (user.getManager() != null) {
            return "manager-" + user.getManager().getId();
        }
        String dept = user.getDepartement();
        if (dept != null && !dept.isBlank()) {
            return "dept-" + normalizeDept(dept);
        }
        return "user-" + user.getId();
    }

    public List<TeamMessageDTO> getMessagesForUser(User user) {
        String teamKey = resolveTeamKey(user);
        List<TeamMessage> recent = teamMessageRepository.findTop50ByTeamKeyOrderBySentAtDesc(teamKey);
        Collections.reverse(recent);
        return recent.stream().map(m -> toDto(m, user)).collect(Collectors.toList());
    }

    public TeamMessageDTO sendMessage(User sender, String content) {
        String trimmed = validateContent(content);

        TeamMessage message = new TeamMessage();
        message.setTeamKey(resolveTeamKey(sender));
        message.setSender(sender);
        message.setContent(trimmed);
        message.setSentAt(LocalDateTime.now());

        return toDto(teamMessageRepository.save(message), sender);
    }

    public TeamMessageDTO updateMessage(User user, Long messageId, String content) {
        String trimmed = validateContent(content);
        TeamMessage message = getOwnedModifiableMessage(user, messageId);

        message.setContent(trimmed);
        message.setEditedAt(LocalDateTime.now());

        return toDto(teamMessageRepository.save(message), user);
    }

    public void deleteMessage(User user, Long messageId) {
        TeamMessage message = getOwnedModifiableMessage(user, messageId);
        teamMessageRepository.delete(message);
    }

    private TeamMessage getOwnedModifiableMessage(User user, Long messageId) {
        TeamMessage message = teamMessageRepository.findByIdWithSender(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable"));

        if (!canAccessTeam(user, message.getTeamKey())) {
            throw new IllegalArgumentException("Accès refusé à ce message");
        }
        Long senderId = message.getSender() != null ? message.getSender().getId() : null;
        if (!Objects.equals(senderId, user.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez modifier que vos propres messages");
        }
        if (!isWithinModifyWindow(message)) {
            throw new IllegalArgumentException(
                    "Modification impossible : le délai de " + MODIFY_WINDOW_MINUTES + " minutes est dépassé");
        }
        return message;
    }

    public boolean canAccessTeam(User user, String teamKey) {
        return teamKey != null && teamKey.equals(resolveTeamKey(user));
    }

    public boolean isWithinModifyWindow(TeamMessage message) {
        if (message.getSentAt() == null) {
            return false;
        }
        return message.getSentAt().plusMinutes(MODIFY_WINDOW_MINUTES).isAfter(LocalDateTime.now());
    }

    private boolean canModify(TeamMessage message, User user) {
        if (message.getSender() == null || user == null) {
            return false;
        }
        if (!message.getSender().getId().equals(user.getId())) {
            return false;
        }
        return isWithinModifyWindow(message);
    }

    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 4000) {
            throw new IllegalArgumentException("Message trop long (max 4000 caractères)");
        }
        return trimmed;
    }

    private TeamMessageDTO toDto(TeamMessage m, User viewer) {
        TeamMessageDTO dto = new TeamMessageDTO();
        dto.setId(m.getId());
        dto.setTeamKey(m.getTeamKey());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());
        dto.setEditedAt(m.getEditedAt());
        dto.setCanModify(canModify(m, viewer));
        if (m.getSender() != null) {
            dto.setSenderId(m.getSender().getId());
            dto.setSenderName(m.getSender().getPrenom() + " " + m.getSender().getNom());
            dto.setSenderRole(m.getSender().getRole() != null ? m.getSender().getRole().name() : null);
        }
        return dto;
    }

    private static String normalizeDept(String dept) {
        return dept.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
    }
}

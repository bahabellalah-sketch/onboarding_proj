package com.onboarding.controller;

import com.onboarding.entity.Notification;
import com.onboarding.entity.NotificationType;
import com.onboarding.entity.User;
import com.onboarding.repository.UserRepository;
import com.onboarding.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all notifications for current user
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        try {
            // Get current authenticated user
            User currentUser = getCurrentUser();
            System.out.println("DEBUG: Current user in notifications endpoint: " + (currentUser != null ? currentUser.getId() + " - " + currentUser.getEmail() : "null"));
            
            if (currentUser == null) {
                // For testing purposes, return empty list if no user is authenticated
                System.out.println("DEBUG: No authenticated user found, returning empty list");
                return ResponseEntity.ok(List.of());
            }
            
            List<Notification> notifications = notificationService.getNotificationsForUser(currentUser.getId());
            System.out.println("DEBUG: Found " + notifications.size() + " notifications for user " + currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.out.println("DEBUG: Error in notifications endpoint: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get unread notifications count for current user
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            long unreadCount = notificationService.getUnreadNotificationsCount(currentUser.getId());
            return ResponseEntity.ok(unreadCount);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Mark all notifications as read for current user
     */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            notificationService.markAllNotificationsAsRead(currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Send custom notification (for admin use)
     */
    @PostMapping("/send")
    public ResponseEntity<Void> sendCustomNotification(@RequestParam String message, 
                                                     @RequestParam NotificationType type,
                                                     @RequestParam(required = false) Long userId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || !currentUser.getRole().name().equals("ADMINISTRATEUR")) {
                return ResponseEntity.status(403).build();
            }
            
            notificationService.sendSystemAlert(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Get notifications by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable NotificationType type) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<Notification> notifications = notificationService.getNotificationsByTypeAndUserId(
                    type, currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                
                // Handle User entity directly
                if (principal instanceof User) {
                    return (User) principal;
                }
                
                // Handle UserDetails (from JWT filter)
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails = 
                        (org.springframework.security.core.userdetails.UserDetails) principal;
                    String email = userDetails.getUsername();
                    return userRepository.findByEmail(email).orElse(null);
                }
                
                // Handle String (email)
                if (principal instanceof String) {
                    String email = (String) principal;
                    return userRepository.findByEmail(email).orElse(null);
                }
            }
            
            return null;
        } catch (Exception e) {
            System.out.println("DEBUG: Error getting current user: " + e.getMessage());
            return null;
        }
    }
}

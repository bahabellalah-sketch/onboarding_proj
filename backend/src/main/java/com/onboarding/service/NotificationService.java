package com.onboarding.service;

import com.onboarding.entity.Assignment;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.Evaluation;
import com.onboarding.entity.Notification;
import com.onboarding.entity.NotificationType;
import com.onboarding.entity.Role;
import com.onboarding.entity.StatutAssignment;
import com.onboarding.entity.User;
import com.onboarding.repository.AssignmentRepository;
import com.onboarding.repository.NotificationRepository;
import com.onboarding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;

    private void notifyOnce(String entityType, Long entityId, Long userId, String message,
                            NotificationType type, Long senderId) {
        if (userId == null) {
            return;
        }
        if (entityType != null && entityId != null
                && notificationRepository.existsByEntityTypeAndEntityIdAndUserId(entityType, entityId, userId)) {
            return;
        }
        Notification notification = new Notification(message, type, userId);
        if (entityType != null) {
            notification.setEntityType(entityType);
        }
        if (entityId != null) {
            notification.setEntityId(entityId);
        }
        if (senderId != null) {
            notification.setSenderId(senderId);
        }
        notificationRepository.save(notification);
    }

    /**
     * Notify collaborateur when a parcours is assigned to them.
     */
    public void sendAssignmentAssignedNotification(Assignment assignment) {
        try {
            User collaborateur = assignment.getUser();
            if (collaborateur == null || assignment.getParcours() == null) {
                return;
            }
            String parcoursNom = assignment.getParcours().getNom();
            String message = "Un nouveau parcours vous a été assigné : \"" + parcoursNom + "\".";
            if (assignment.getDateFinPrevisionnelle() != null) {
                message += " Date limite : " + assignment.getDateFinPrevisionnelle() + ".";
            }
            notifyOnce("ASSIGNMENT_ASSIGNED", assignment.getId(), collaborateur.getId(),
                    message, NotificationType.ASSIGNMENT_ASSIGNED, null);
        } catch (Exception e) {
            System.out.println("Error sending assignment assigned notification: " + e.getMessage());
        }
    }

    /**
     * Notify manager when a collaborateur completes a checklist step.
     */
    public void sendEtapeCompleteNotification(Checklist checklist, User collaborateur) {
        try {
            User manager = collaborateur.getManager();
            if (manager == null) {
                return;
            }
            String titre = checklist.getTitre() != null ? checklist.getTitre() : "Étape";
            String message = collaborateur.getPrenom() + " " + collaborateur.getNom()
                    + " a terminé l'étape : \"" + titre + "\".";
            notifyOnce("ETAPE_COMPLETE", checklist.getId(), manager.getId(),
                    message, NotificationType.ETAPE_COMPLETE, collaborateur.getId());
        } catch (Exception e) {
            System.out.println("Error sending etape complete notification: " + e.getMessage());
        }
    }

    /**
     * Notify collaborateur when they complete an entire parcours.
     */
    public void sendParcoursCompleteNotification(Assignment assignment, User collaborateur) {
        try {
            if (collaborateur == null || assignment.getParcours() == null) {
                return;
            }
            String message = "Félicitations ! Vous avez terminé le parcours \""
                    + assignment.getParcours().getNom() + "\".";
            notifyOnce("PARCOURS_COMPLETE_COLLAB", assignment.getId(), collaborateur.getId(),
                    message, NotificationType.PARCOURS_COMPLETE, null);

            User manager = collaborateur.getManager();
            if (manager != null) {
                String managerMessage = collaborateur.getPrenom() + " " + collaborateur.getNom()
                        + " a terminé le parcours \"" + assignment.getParcours().getNom() + "\".";
                notifyOnce("PARCOURS_COMPLETE_MGR", assignment.getId(), manager.getId(),
                        managerMessage, NotificationType.MANAGER_NOTIFICATION, collaborateur.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending parcours complete notification: " + e.getMessage());
        }
    }

    /**
     * Remind collaborateur and manager before assignment due date (7, 3, 1 days).
     */
    public void sendDeadlineReminders() {
        try {
            LocalDate today = LocalDate.now();
            for (Assignment assignment : assignmentRepository.findAll()) {
                if (assignment.getDateFinPrevisionnelle() == null
                        || assignment.getUser() == null
                        || assignment.getParcours() == null) {
                    continue;
                }
                StatutAssignment statut = assignment.getStatut();
                if (statut == StatutAssignment.TERMINE || statut == StatutAssignment.ANNULE) {
                    continue;
                }
                long daysLeft = ChronoUnit.DAYS.between(today, assignment.getDateFinPrevisionnelle());
                if (daysLeft != 7 && daysLeft != 3 && daysLeft != 1) {
                    continue;
                }
                User collaborateur = assignment.getUser();
                String parcoursNom = assignment.getParcours().getNom();
                String suffix = daysLeft == 1 ? "demain" : "dans " + daysLeft + " jours";
                notifyOnce("DEADLINE_REMINDER_" + daysLeft + "_C", assignment.getId(), collaborateur.getId(),
                        "Rappel : le parcours \"" + parcoursNom + "\" se termine " + suffix + ".",
                        NotificationType.DEADLINE_REMINDER, null);

                User manager = collaborateur.getManager();
                if (manager != null) {
                    notifyOnce("DEADLINE_REMINDER_" + daysLeft + "_M", assignment.getId(), manager.getId(),
                            "Échéance " + suffix + " pour " + collaborateur.getPrenom() + " "
                                    + collaborateur.getNom() + " — \"" + parcoursNom + "\".",
                            NotificationType.MANAGER_NOTIFICATION, null);
                }
            }
        } catch (Exception e) {
            System.out.println("Error sending deadline reminders: " + e.getMessage());
        }
    }

    /**
     * Notify collaborateur when their manager signs a required document.
     */
    public void sendDocumentSignedNotification(User collaborateur, String documentName, Long documentId) {
        try {
            if (collaborateur == null) {
                return;
            }
            notifyOnce("DOCUMENT_SIGNED", documentId, collaborateur.getId(),
                    "Votre document \"" + documentName + "\" a été signé par votre manager.",
                    NotificationType.DOCUMENT_SIGNED, null);
        } catch (Exception e) {
            System.out.println("Error sending document signed notification: " + e.getMessage());
        }
    }

    /**
     * Notify manager when a collaborateur submits an evaluation.
     */
    public void sendEvaluationReceivedNotification(Evaluation evaluation) {
        try {
            Checklist checklist = evaluation.getChecklist();
            if (checklist == null || checklist.getAssignment() == null) {
                return;
            }
            User collaborateur = checklist.getAssignment().getUser();
            if (collaborateur == null) {
                return;
            }
            User manager = collaborateur.getManager();
            if (manager == null) {
                return;
            }
            String titre = checklist.getTitre() != null ? checklist.getTitre() : "une étape";
            String message = collaborateur.getPrenom() + " " + collaborateur.getNom()
                    + " a évalué \"" + titre + "\" (" + evaluation.getRating() + "/5).";
            notifyOnce("EVALUATION_RECEIVED", evaluation.getId(), manager.getId(),
                    message, NotificationType.EVALUATION_RECEIVED, collaborateur.getId());
        } catch (Exception e) {
            System.out.println("Error sending evaluation notification: " + e.getMessage());
        }
    }

    /**
     * Send notification for assignment completion to manager
     */
    public void sendAssignmentCompletionNotification(Assignment assignment, User collaborateur) {
        try {
            System.out.println("DEBUG: Attempting to send assignment completion notification");
            System.out.println("DEBUG: Assignment ID: " + assignment.getId() + ", Title: " + assignment.getTitre());
            System.out.println("DEBUG: Collaborateur ID: " + collaborateur.getId() + ", Name: " + collaborateur.getPrenom() + " " + collaborateur.getNom());
            
            User manager = collaborateur.getManager();
            if (manager == null) {
                System.out.println("DEBUG: No manager found for collaborateur: " + collaborateur.getId());
                return;
            }
            
            System.out.println("DEBUG: Manager found: " + manager.getId() + ", Name: " + manager.getPrenom() + " " + manager.getNom());
            
            // Check if notification already exists
            System.out.println("DEBUG: Checking if notification already exists for ASSIGNMENT, ID: " + assignment.getId() + ", Manager ID: " + manager.getId());
            boolean notificationExists = notificationRepository.existsByEntityTypeAndEntityIdAndUserId(
                "ASSIGNMENT", assignment.getId(), manager.getId());
            System.out.println("DEBUG: Notification exists: " + notificationExists);
            
            if (!notificationExists) {
                System.out.println("DEBUG: Creating new notification");
                Notification notification = new Notification(
                    "Assignement complété: " + assignment.getTitre(),
                    NotificationType.ASSIGNMENT_COMPLETE,
                    manager.getId()
                );
                notification.setEntityType("ASSIGNMENT");
                notification.setEntityId(assignment.getId());
                notificationRepository.save(notification);
                
                System.out.println("Notification sent for assignment completion: " + assignment.getTitre() + 
                                 " to manager: " + manager.getId());
            } else {
                System.out.println("DEBUG: Notification already exists, skipping");
            }
        } catch (Exception e) {
            System.out.println("Error sending assignment completion notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for overdue assignments to collaborateur and manager
     */
    public void sendOverdueAssignmentNotification(Assignment assignment, User collaborateur) {
        try {
            User manager = collaborateur.getManager();
            if (manager == null) {
                System.out.println("No manager found for collaborateur: " + collaborateur.getId());
                return;
            }
            
            String titre = assignment.getParcours() != null ? assignment.getParcours().getNom() : "parcours";
            notifyOnce("ASSIGNMENT_OVERDUE_C", assignment.getId(), collaborateur.getId(),
                    "Parcours en retard : \"" + titre + "\".",
                    NotificationType.ASSIGNMENT_OVERDUE, null);
            notifyOnce("ASSIGNMENT_OVERDUE_M", assignment.getId(), manager.getId(),
                    "Parcours en retard pour " + collaborateur.getPrenom() + " " + collaborateur.getNom()
                            + " : \"" + titre + "\".",
                    NotificationType.ASSIGNMENT_OVERDUE, null);
            
            System.out.println("Overdue notification sent to collaborateur: " + collaborateur.getId() + 
                             " and manager: " + manager.getId());
        } catch (Exception e) {
            System.out.println("Error sending overdue notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for new user creation to admin
     */
    public void sendUserCreationNotification(User user) {
        try {
            // Find admin users
            List<User> admins = userRepository.findByRole(Role.ADMINISTRATEUR);
            
            for (User admin : admins) {
                Notification notification = new Notification(
                        "Nouvel utilisateur créé: " + user.getPrenom() + " " + user.getNom(),
                        NotificationType.USER_CREATED,
                        admin.getId()
                );
                notification.setEntityType("USER");
                notification.setEntityId(user.getId());
                notificationRepository.save(notification);
                
                System.out.println("User creation notification sent to admin: " + admin.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending user creation notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for new user creation to manager
     */
    public void sendUserCreationNotificationToManager(User user) {
        try {
            User manager = user.getManager();
            if (manager == null) {
                System.out.println("No manager found for user: " + user.getId());
                return;
            }
            
            // Check if notification already exists
            boolean notificationExists = notificationRepository.existsByEntityTypeAndEntityIdAndUserId(
                "USER_MANAGER", user.getId(), manager.getId());
            
            if (!notificationExists) {
                Notification notification = new Notification(
                    "Nouvel collaborateur ajouté: " + user.getPrenom() + " " + user.getNom() + " (" + user.getEmail() + ")",
                    NotificationType.USER_CREATED,
                    manager.getId()
                );
                notification.setEntityType("USER_MANAGER");
                notification.setEntityId(user.getId());
                notificationRepository.save(notification);
                
                System.out.println("User creation notification sent to manager: " + manager.getId() + " for user: " + user.getId());
            } else {
                System.out.println("User creation notification already exists for manager: " + manager.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending user creation notification to manager: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for user update to admin
     */
    public void sendUserUpdateNotification(User user) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMINISTRATEUR);
            
            for (User admin : admins) {
                Notification notification = new Notification(
                        "Utilisateur mis à jour: " + user.getPrenom() + " " + user.getNom(),
                        NotificationType.USER_UPDATED,
                        admin.getId()
                );
                notification.setEntityType("USER");
                notification.setEntityId(user.getId());
                notificationRepository.save(notification);
                
                System.out.println("User update notification sent to admin: " + admin.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending user update notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for user deletion to admin
     */
    public void sendUserDeletionNotification(User user) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMINISTRATEUR);
            
            for (User admin : admins) {
                Notification notification = new Notification(
                        "Utilisateur supprimé: " + user.getPrenom() + " " + user.getNom(),
                        NotificationType.USER_DELETED,
                        admin.getId()
                );
                notification.setEntityType("USER");
                notification.setEntityId(user.getId());
                notificationRepository.save(notification);
                
                System.out.println("User deletion notification sent to admin: " + admin.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending user deletion notification: " + e.getMessage());
        }
    }
    
    /**
     * Send system alert to all admin users
     */
    public void sendSystemAlert(String message) {
        try {
            List<User> admins = userRepository.findByRole(Role.ADMINISTRATEUR);
            
            for (User admin : admins) {
                Notification notification = new Notification(
                        message,
                        NotificationType.SYSTEM_ALERT,
                        admin.getId()
                );
                notificationRepository.save(notification);
                
                System.out.println("System alert sent to admin: " + admin.getId());
            }
        } catch (Exception e) {
            System.out.println("Error sending system alert: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for document upload
     */
    public void sendDocumentUploadNotification(User uploader, String documentName, Long documentId) {
        try {
            if (uploader == null) {
                return;
            }
            User manager = uploader.getManager();
            if (manager == null) {
                return;
            }
            notifyOnce("DOCUMENT_UPLOADED", documentId, manager.getId(),
                    uploader.getPrenom() + " " + uploader.getNom()
                            + " a déposé le document \"" + documentName + "\".",
                    NotificationType.DOCUMENT_UPLOADED, uploader.getId());
        } catch (Exception e) {
            System.out.println("Error sending document upload notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification for missing document
     */
    public void sendMissingDocumentNotification(Long userId, String documentName) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return;
            }
            
            User manager = user.getManager();
            if (manager == null) {
                return;
            }
            
            Notification notification = new Notification(
                    "Document manquant: " + documentName,
                    NotificationType.DOCUMENT_MISSING,
                    manager.getId()
            );
            notification.setEntityType("DOCUMENT");
            notification.setEntityId(userId);
            notificationRepository.save(notification);
            
            System.out.println("Missing document notification sent to manager: " + manager.getId());
        } catch (Exception e) {
            System.out.println("Error sending missing document notification: " + e.getMessage());
        }
    }
    
    /**
     * Send notification when a checklist is blocked
     */
    public void sendChecklistBloqueeNotification(com.onboarding.entity.Checklist checklist, User collaborateur) {
        try {
            User manager = collaborateur.getManager();
            if (manager == null) {
                System.out.println("No manager found for checklist blocked notification");
                return;
            }
            
            notifyOnce("CHECKLIST_BLOQUEE", checklist.getId(), manager.getId(),
                    "Tâche bloquée : \"" + checklist.getTitre() + "\" pour "
                            + collaborateur.getPrenom() + " " + collaborateur.getNom() + ".",
                    NotificationType.CHECKLIST_BLOQUEE, collaborateur.getId());
        } catch (Exception e) {
            System.out.println("Error sending checklist bloquée notification: " + e.getMessage());
        }
    }
    
    /**
     * Mark notification as read
     */
    public void markNotificationAsRead(Long notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null) {
                notification.setRead(true);
                notification.setDateLecture(LocalDateTime.now());
                notificationRepository.save(notification);
                
                System.out.println("Notification marked as read: " + notificationId);
            }
        } catch (Exception e) {
            System.out.println("Error marking notification as read: " + e.getMessage());
        }
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            System.out.println("Notification deleted: " + notificationId);
        } catch (Exception e) {
            System.out.println("Error deleting notification: " + e.getMessage());
        }
    }
    
    /**
     * Get notifications for a specific user
     */
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(userId);
    }
    
    /**
     * Get unread notifications count for a user
     */
    public Long getUnreadNotificationsCount(Long userId) {
        try {
            List<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsByUserId(userId);
            return (long) unreadNotifications.size();
        } catch (Exception e) {
            System.out.println("Error getting unread notifications count: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllNotificationsAsRead(Long userId) {
        try {
            List<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsByUserId(userId);
            
            for (Notification notification : unreadNotifications) {
                notification.setRead(true);
                notification.setDateLecture(LocalDateTime.now());
            }
            notificationRepository.saveAll(unreadNotifications);
            System.out.println("All notifications marked as read for user: " + userId);
        } catch (Exception e) {
            System.out.println("Error marking all notifications as read: " + e.getMessage());
        }
    }
    
    /**
     * Get notifications by type and user ID
     */
    public List<Notification> getNotificationsByTypeAndUserId(NotificationType type, Long userId) {
        try {
            return notificationRepository.findByEntityTypeAndUserIdOrderByDateCreationDesc(type.name(), userId);
        } catch (Exception e) {
            System.out.println("Error getting notifications by type and user: " + e.getMessage());
            return List.of();
        }
    }
}

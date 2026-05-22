package com.onboarding.config;

import com.onboarding.service.AssignmentService;
import com.onboarding.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateAssignmentStatusesDaily() {
        System.out.println("=== Mise à jour quotidienne automatique des statuts d'assignations - " + java.time.LocalDate.now() + " ===");
        assignmentService.checkAndUpdateOverdueAssignments();
        notificationService.sendDeadlineReminders();
        System.out.println("=== Mise à jour des statuts d'assignations terminée ===");
    }

    // Exécuter toutes les heures pour vérifier les parcours en retard (optionnel)
    @Scheduled(cron = "0 0 * * * ?")
    public void checkOverdueAssignmentsHourly() {
        // Optionnel : décommenter si vous voulez une vérification plus fréquente
        // assignmentService.checkAndUpdateOverdueAssignments();
    }
}

package com.onboarding.repository;

import com.onboarding.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by user ID ordered by creation date (newest first)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.dateCreation DESC")
    List<Notification> findByUserIdOrderByDateCreationDesc(Long userId);
    
    /**
     * Find unread notifications for a user
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.read = false ORDER BY n.dateCreation DESC")
    List<Notification> findUnreadNotificationsByUserId(Long userId);
    
    /**
     * Check if notification exists for specific entity, entity ID, and user ID
     */
    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.entityType = :entityType AND n.entityId = :entityId AND n.userId = :userId")
    boolean existsByEntityTypeAndEntityIdAndUserId(String entityType, Long entityId, Long userId);
    
    /**
     * Find notifications by entity type and user ID
     */
    @Query("SELECT n FROM Notification n WHERE n.entityType = :entityType AND n.userId = :userId ORDER BY n.dateCreation DESC")
    List<Notification> findByEntityTypeAndUserIdOrderByDateCreationDesc(String entityType, Long userId);
    
    /**
     * Find notifications by sender ID
     */
    List<Notification> findBySenderId(Long senderId);
    
    /**
     * Delete notifications by user ID
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteByUserId(Long userId);
    
    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.dateLecture = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.read = false")
    void markAllAsReadByUserId(Long userId);
    
    /**
     * Find notifications by read status
     */
    List<Notification> findByRead(Boolean read);
}

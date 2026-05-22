package com.onboarding.repository;

import com.onboarding.entity.AuditLog;
import com.onboarding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUser(User user);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByEffectuePar(String effectuePar);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user = :user AND a.dateAction BETWEEN :start AND :end ORDER BY a.dateAction DESC")
    List<AuditLog> findByUserAndDateActionBetween(@Param("user") User user, 
                                                 @Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM AuditLog a WHERE a.dateAction BETWEEN :start AND :end ORDER BY a.dateAction DESC")
    List<AuditLog> findByDateActionBetween(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
}

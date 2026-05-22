package com.onboarding.repository;

import com.onboarding.entity.EmailVerification;
import com.onboarding.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByVerificationToken(String token);
    
    Optional<EmailVerification> findByUser(User user);
    
    void deleteByUser(User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification e WHERE e.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}

package com.onboarding.repository;

import com.onboarding.entity.User;
import com.onboarding.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByEmail(String email);
    
    Optional<UserProfile> findByUser(User user);
    
    boolean existsByEmail(String email);
    
    void deleteByUser(User user);
}

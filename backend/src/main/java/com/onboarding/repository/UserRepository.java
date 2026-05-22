package com.onboarding.repository;

import com.onboarding.entity.User;
import com.onboarding.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByStatut(Boolean statut);
    
    List<User> findByDepartement(String departement);
    
    List<User> findByManagerId(Long managerId);
    
    List<User> findByManager(User manager);
    
    @Query("SELECT u FROM User u WHERE u.resetToken = :token AND u.resetTokenExpiry > :now")
    Optional<User> findByResetTokenAndExpiryAfter(@Param("token") String token, @Param("now") java.time.LocalDateTime now);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.prenom LIKE %:keyword% OR u.nom LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.managedEmployees WHERE u.id = :managerId")
    Optional<User> findByIdWithManagedEmployees(@Param("managerId") Long managerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.manager WHERE u.id = :id")
    Optional<User> findByIdWithManager(@Param("id") Long id);
}

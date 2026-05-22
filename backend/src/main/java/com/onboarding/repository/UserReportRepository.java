package com.onboarding.repository;

import com.onboarding.entity.ReportStatus;
import com.onboarding.entity.User;
import com.onboarding.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
    List<UserReport> findByReportedUser(User reportedUser);
    
    List<UserReport> findByReporter(User reporter);
    
    List<UserReport> findByStatus(ReportStatus status);
    
    List<UserReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);
    
    @Query("SELECT ur FROM UserReport ur WHERE ur.reportedUser.id = :userId ORDER BY ur.createdAt DESC")
    List<UserReport> findReportsByUserId(Long userId);
    
    boolean existsByReportedUserAndReporterAndStatus(
        User reportedUser, 
        User reporter, 
        ReportStatus status
    );
}

package com.onboarding.repository;

import com.onboarding.entity.TeamMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMessageRepository extends JpaRepository<TeamMessage, Long> {

    List<TeamMessage> findByTeamKeyOrderBySentAtAsc(String teamKey);

    List<TeamMessage> findTop50ByTeamKeyOrderBySentAtDesc(String teamKey);

    @Query("SELECT m FROM TeamMessage m JOIN FETCH m.sender WHERE m.id = :id")
    Optional<TeamMessage> findByIdWithSender(@Param("id") Long id);
}

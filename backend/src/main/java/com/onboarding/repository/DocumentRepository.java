package com.onboarding.repository;

import com.onboarding.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEtapeId(Long etapeId);
}

package com.onboarding.repository;

import com.onboarding.entity.DocumentSignatureAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentSignatureAuditRepository extends JpaRepository<DocumentSignatureAudit, Long> {
    
    List<DocumentSignatureAudit> findByDocumentIdOrderBySignatureDateDesc(Long documentId);
    
    Optional<DocumentSignatureAudit> findByDocumentIdAndUserId(Long documentId, Long userId);
    
    @Query("SELECT COUNT(a) > 0 FROM DocumentSignatureAudit a WHERE a.documentId = :documentId")
    boolean existsByDocumentId(@Param("documentId") Long documentId);
    
    @Query("SELECT a FROM DocumentSignatureAudit a WHERE a.documentHash = :hash")
    List<DocumentSignatureAudit> findByDocumentHash(@Param("hash") String hash);
}

package com.onboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_signature_audit")
public class DocumentSignatureAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "signature_date", nullable = false)
    private LocalDateTime signatureDate;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "document_hash", nullable = false)
    private String documentHash;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "signature_type", nullable = false)
    private String signatureType = "SES"; // Simple Electronic Signature

    // Constructors
    public DocumentSignatureAudit() {}

    public DocumentSignatureAudit(Long documentId, Long userId, String userEmail, String userName, 
                                  String ipAddress, String documentHash, String userAgent) {
        this.documentId = documentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.signatureDate = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.documentHash = documentHash;
        this.userAgent = userAgent;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getSignatureDate() { return signatureDate; }
    public void setSignatureDate(LocalDateTime signatureDate) { this.signatureDate = signatureDate; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String documentHash) { this.documentHash = documentHash; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getSignatureType() { return signatureType; }
    public void setSignatureType(String signatureType) { this.signatureType = signatureType; }
}

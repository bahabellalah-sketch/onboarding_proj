package com.onboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etape_id", nullable = false)
    private Long etapeId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_signed", nullable = false)
    private Boolean isSigned = false;

    @Column(name = "signature_date")
    private LocalDateTime signatureDate;

    @Column(name = "uploaded_by_name", nullable = false)
    private String uploadedByName;

    @Column(name = "signed_by_user_id")
    private Long signedByUserId;

    @Column(name = "signed_by_user_email")
    private String signedByUserEmail;

    @Column(name = "signed_by_user_name")
    private String signedByUserName;

    @Column(name = "signature_ip_address")
    private String signatureIpAddress;

    @Column(name = "document_hash")
    private String documentHash;

    @Column(name = "signature_user_agent")
    private String signatureUserAgent;

    @Column(name = "signature_type")
    private String signatureType = "SES";

    @JsonIgnore
    @Lob
    @Column(name = "content", columnDefinition = "LONGBLOB")
    private byte[] content;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEtapeId() { return etapeId; }
    public void setEtapeId(Long etapeId) { this.etapeId = etapeId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public Boolean getIsSigned() { return isSigned; }
    public void setIsSigned(Boolean isSigned) { this.isSigned = isSigned; }

    public LocalDateTime getSignatureDate() { return signatureDate; }
    public void setSignatureDate(LocalDateTime signatureDate) { this.signatureDate = signatureDate; }

    public String getUploadedByName() { return uploadedByName; }
    public void setUploadedByName(String uploadedByName) { this.uploadedByName = uploadedByName; }

    public Long getSignedByUserId() { return signedByUserId; }
    public void setSignedByUserId(Long signedByUserId) { this.signedByUserId = signedByUserId; }

    public String getSignedByUserEmail() { return signedByUserEmail; }
    public void setSignedByUserEmail(String signedByUserEmail) { this.signedByUserEmail = signedByUserEmail; }

    public String getSignedByUserName() { return signedByUserName; }
    public void setSignedByUserName(String signedByUserName) { this.signedByUserName = signedByUserName; }

    public String getSignatureIpAddress() { return signatureIpAddress; }
    public void setSignatureIpAddress(String signatureIpAddress) { this.signatureIpAddress = signatureIpAddress; }

    public String getDocumentHash() { return documentHash; }
    public void setDocumentHash(String documentHash) { this.documentHash = documentHash; }

    public String getSignatureUserAgent() { return signatureUserAgent; }
    public void setSignatureUserAgent(String signatureUserAgent) { this.signatureUserAgent = signatureUserAgent; }

    public String getSignatureType() { return signatureType; }
    public void setSignatureType(String signatureType) { this.signatureType = signatureType; }

    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
}

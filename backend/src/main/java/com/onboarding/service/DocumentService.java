package com.onboarding.service;

import com.onboarding.entity.Document;
import com.onboarding.entity.DocumentSignatureAudit;
import com.onboarding.entity.User;
import com.onboarding.entity.Checklist;
import com.onboarding.repository.ChecklistRepository;
import com.onboarding.repository.DocumentRepository;
import com.onboarding.repository.DocumentSignatureAuditRepository;
import com.onboarding.repository.UserRepository;
import com.onboarding.util.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentSignatureAuditRepository auditRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChecklistRepository checklistRepository;

    @Value("${app.document.max-size:104857600}")
    private long maxDocumentSizeBytes;

    public List<Document> getDocumentsByEtapeId(Long etapeId) {
        return documentRepository.findByEtapeId(etapeId);
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, Long etapeId) {
        if (file.getSize() > maxDocumentSizeBytes) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Le fichier dépasse la taille maximale autorisée.");
        }
        try {
            byte[] content = file.getBytes();
            String documentHash = CryptoUtils.generateSHA256Hash(content);

            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "document";
            }
            
            Document document = new Document();
            document.setEtapeId(etapeId);
            document.setOriginalFilename(originalName);
            document.setFilename(originalName);
            String mime = file.getContentType();
            document.setFileType(mime != null && !mime.isBlank() ? mime : "application/octet-stream");
            document.setFileSize(file.getSize());
            document.setContent(content);
            document.setUploadDate(LocalDateTime.now());
            document.setIsSigned(false);
            document.setDocumentHash(documentHash);
            
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("DEBUG: Authentication = " + authentication);
            
            if (authentication != null) {
                System.out.println("DEBUG: Authentication principal = " + authentication.getPrincipal());
                System.out.println("DEBUG: Authentication principal class = " + authentication.getPrincipal().getClass().getName());
                
                if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
                    String email = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
                    System.out.println("DEBUG: User email from security context = " + email);
                    
                    try {
                        User user = userRepository.findByEmail(email).orElse(null);
                        if (user != null) {
                            String fullName = (user.getPrenom() != null ? user.getPrenom() : "") + " " + (user.getNom() != null ? user.getNom() : "");
                            fullName = fullName.trim();
                            if (fullName.isEmpty()) {
                                fullName = email != null && !email.isBlank() ? email : "Utilisateur";
                            }
                            document.setUploadedByName(fullName);
                            System.out.println("DEBUG: Set uploadedByName to = " + fullName);
                        } else {
                            System.out.println("DEBUG: User not found for email: " + email);
                            document.setUploadedByName(email); // Fallback to email
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error finding user: " + e.getMessage());
                        document.setUploadedByName(email); // Fallback to email
                    }
                } else {
                    System.out.println("DEBUG: Principal is not UserDetails, using name: " + authentication.getName());
                    document.setUploadedByName(authentication.getName());
                }
            } else {
                System.out.println("DEBUG: No authentication found, using 'Unknown User'");
                document.setUploadedByName("Unknown User");
            }
            
            Document saved = documentRepository.save(document);
            User uploader = null;
            if (authentication != null && authentication.getPrincipal()
                    instanceof org.springframework.security.core.userdetails.UserDetails) {
                String email = ((org.springframework.security.core.userdetails.UserDetails)
                        authentication.getPrincipal()).getUsername();
                uploader = userRepository.findByEmail(email).orElse(null);
            }
            if (uploader != null) {
                notificationService.sendDocumentUploadNotification(
                        uploader, saved.getOriginalFilename(), saved.getId());
            }
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }

    @Transactional
    public Document signDocument(Long documentId, HttpServletRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        // Check if document is already signed
        if (document.getIsSigned()) {
            throw new RuntimeException("Document is already signed");
        }
        
        // Verify document integrity with hash
        if (document.getDocumentHash() != null && !CryptoUtils.verifyHash(document.getContent(), document.getDocumentHash())) {
            throw new RuntimeException("Document integrity check failed - content has been modified");
        }
        
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            throw new RuntimeException("User not authenticated");
        }
        
        String email = ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));
        
        // Get client IP address
        String ipAddress = getClientIpAddress(request);
        
        // Get user agent
        String userAgent = request.getHeader("User-Agent");
        
        // Update document with signature information
        document.setIsSigned(true);
        document.setSignatureDate(LocalDateTime.now());
        document.setSignedByUserId(user.getId());
        document.setSignedByUserEmail(user.getEmail());
        document.setSignedByUserName(user.getPrenom() + " " + user.getNom());
        document.setSignatureIpAddress(ipAddress);
        document.setSignatureUserAgent(userAgent);
        document.setSignatureType("SES");
        
        // Create audit trail entry
        DocumentSignatureAudit auditEntry = new DocumentSignatureAudit(
            documentId,
            user.getId(),
            user.getEmail(),
            user.getPrenom() + " " + user.getNom(),
            ipAddress,
            document.getDocumentHash(),
            userAgent
        );
        
        auditRepository.save(auditEntry);
        Document signed = documentRepository.save(document);

        if (signed.getEtapeId() != null) {
            for (Checklist checklist : checklistRepository.findByEtapeId(signed.getEtapeId())) {
                if (checklist.getAssignment() != null && checklist.getAssignment().getUser() != null) {
                    notificationService.sendDocumentSignedNotification(
                            checklist.getAssignment().getUser(),
                            signed.getOriginalFilename(),
                            signed.getId());
                    break;
                }
            }
        }

        return signed;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        documentRepository.delete(document);
    }

    @Transactional
    public Document downloadDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
}

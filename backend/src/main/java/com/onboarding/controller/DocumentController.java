package com.onboarding.controller;

import com.onboarding.entity.Document;
import com.onboarding.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("/etape/{etapeId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<List<Document>> getDocumentsByEtape(@PathVariable Long etapeId) {
        try {
            List<Document> documents = documentService.getDocumentsByEtapeId(etapeId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("etapeId") Long etapeId
    ) {
        System.out.println("=== DEBUG: Upload called ===");
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("File type: " + file.getContentType());
        System.out.println("Etape ID: " + etapeId);
        
        try {
            Document document = documentService.uploadDocument(file, etapeId);
            return ResponseEntity.ok(document);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("ERROR during upload: " + e.getMessage());
            e.printStackTrace();
            String detail = rootCauseMessage(e);
            return ResponseEntity.status(500).contentType(MediaType.TEXT_PLAIN).body(detail);
        }
    }

    private static String rootCauseMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        String m = t.getMessage();
        if (m == null || m.isBlank()) {
            return "Erreur lors de l'enregistrement du document.";
        }
        if (m.length() > 500) {
            return m.substring(0, 500) + "…";
        }
        return m;
    }

    @PostMapping("/{documentId}/sign")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER')")
    public ResponseEntity<Document> signDocument(@PathVariable Long documentId, HttpServletRequest request) {
        try {
            Document document = documentService.signDocument(documentId, request);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<String> deleteDocument(@PathVariable Long documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ResponseEntity.ok("Document supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression");
        }
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR', 'MANAGER', 'COLLABORATEUR')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        try {
            Document document = documentService.downloadDocument(documentId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + document.getOriginalFilename() + "\"")
                    .body(document.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}

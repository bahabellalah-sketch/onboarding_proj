package com.onboarding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@SuppressWarnings("nullness")
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;
    
    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;
    
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = frontendBaseUrl + "/reset-password?token=" + resetToken;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Réinitialisation de votre mot de passe - Plateforme Onboarding");
        
        String emailBody = "Bonjour,\n\n" +
                "Vous avez demandé la réinitialisation de votre mot de passe pour la plateforme d'onboarding.\n\n" +
                "Veuillez cliquer sur le lien suivant pour définir votre mot de passe :\n" +
                resetUrl + "\n\n" +
                "Ce lien expirera dans 24 heures.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.\n\n" +
                "Cordialement,\n" +
                "L'équipe de la plateforme d'onboarding";
        
        message.setText(emailBody);
        mailSender.send(message);
    }
    
    public void sendWelcomeEmail(String toEmail, String prenom, String resetToken) {
        String resetUrl = frontendBaseUrl + "/set-password?token=" + resetToken;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Bienvenue sur la plateforme d'onboarding");
        
        String emailBody = "Bonjour " + prenom + ",\n\n" +
                "Bienvenue sur la plateforme d'onboarding !\n\n" +
                "Votre compte a été créé avec succès. Pour finaliser votre inscription, veuillez définir votre mot de passe en cliquant sur le lien suivant :\n" +
                resetUrl + "\n\n" +
                "Politique de sécurité pour le mot de passe :\n" +
                "- Minimum 8 caractères\n" +
                "- Au moins une majuscule\n" +
                "- Au moins un chiffre\n\n" +
                "Ce lien expirera dans 24 heures.\n\n" +
                "Cordialement,\n" +
                "L'équipe de la plateforme d'onboarding";
        
        message.setText(emailBody);
        mailSender.send(message);
    }
    
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }
    
    public LocalDateTime calculateTokenExpiry() {
        return LocalDateTime.now().plusHours(24);
    }
    
    public void sendVerificationEmail(String to, String verificationToken) {
        String subject = "Vérification de votre email - Plateforme Onboarding";
        String verificationUrl = frontendBaseUrl + "/verify-email?token=" + verificationToken;
        
        String body = "<html>"
                + "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px;'>"
                + "<h2 style='color: #007bff; text-align: center;'>Vérification de votre email</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Merci de vous être inscrit sur notre plateforme d'onboarding. Pour activer votre compte, veuillez vérifier votre email en cliquant sur le bouton ci-dessous :</p>"
                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='" + verificationUrl + "' "
                + "style='background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;'>"
                + ">Vérifier mon email</a>"
                + "</div>"
                + "<p style='color: #6c757d; font-size: 14px;'>"
                + "Ce lien expirera dans 24 heures. Si vous n'avez pas demandé cette vérification, veuillez ignorer cet email."
                + "</p>"
                + "<hr style='border: none; border-top: 1px solid #dee2e6; margin: 20px 0;'>"
                + "<p style='font-size: 12px; color: #6c757d; text-align: center;'>"
                + "© 2024 Plateforme Onboarding. Tous droits réservés."
                + "</p>"
                + "</div>"
                + "</body>"
                + "</html>";
        
        final String fromParam = from;
        final String toParam = to;
        final String subjectParam = subject;
        final String bodyParam = body;
        
        // Vérifications explicites pour garantir la non-nullité
        if (fromParam == null) {
            throw new IllegalArgumentException("L'adresse d'expéditeur ne peut pas être null");
        }
        if (toParam == null) {
            throw new IllegalArgumentException("L'adresse de destinataire ne peut pas être null");
        }
        
        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setFrom(fromParam);
                helper.setTo(toParam);
                helper.setSubject(subjectParam);
                helper.setText(bodyParam, true); // true pour HTML
            });
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage(), e);
        }
    }
    
    public void sendReportWarningEmail(String toEmail, String userName, String adminNotes, String reportType, String reporterName) {
        String subject = "Avertissement - Signalement sur votre compte - Plateforme Onboarding";
        
        String body = "<html>"
                + "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<div style='background-color: #fff3cd; padding: 20px; border-radius: 8px; border: 2px solid #ffc107;'>"
                + "<h2 style='color: #856404; text-align: center;'>⚠️ Avertissement - Signalement</h2>"
                + "<p>Bonjour " + userName + ",</p>"
                + "<p>Nous vous informons qu'un signalement a été fait concernant votre compte sur la plateforme d'onboarding.</p>"
                + "<div style='background-color: #fff; padding: 15px; border-radius:5px; margin: 20px 0;'>"
                + "<p><strong>Type de signalement :</strong> " + reportType + "</p>"
                + "<p><strong>Signalé par :</strong> " + reporterName + "</p>"
                + "</div>"
                + "<p><strong>Message de l'administrateur :</strong></p>"
                + "<div style='background-color: #f8f9fa; padding: 15px; border-left: 4px solid #ffc107; margin: 15px 0;'>"
                + "<p style='margin: 0;'>" + (adminNotes != null ? adminNotes : "Aucun message supplémentaire.") + "</p>"
                + "</div>"
                + "<p style='color: #856404; font-size: 14px;'>"
                + "Nous vous prions de prendre en compte cet avertissement et de vous conformer aux règles de la plateforme."
                + "</p>"
                + "<hr style='border: none; border-top: 1px solid #dee2e6; margin: 20px 0;'>"
                + "<p style='font-size: 12px; color: #6c757d; text-align: center;'>"
                + "© 2024 Plateforme Onboarding. Tous droits réservés."
                + "</p>"
                + "</div>"
                + "</body>"
                + "</html>";
        
        final String fromParam = from;
        final String toParam = toEmail;
        final String subjectParam = subject;
        final String bodyParam = body;
        
        // Vérifications explicites pour garantir la non-nullité
        if (fromParam == null) {
            throw new IllegalArgumentException("L'adresse d'expéditeur ne peut pas être null");
        }
        if (toParam == null) {
            throw new IllegalArgumentException("L'adresse de destinataire ne peut pas être null");
        }
        
        try {
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setFrom(fromParam);
                helper.setTo(toParam);
                helper.setSubject(subjectParam);
                helper.setText(bodyParam, true); // true pour HTML
            });
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'avertissement: " + e.getMessage(), e);
        }
    }
    
    public void sendReportApprovedEmail(String toEmail, String userName, String adminNotes, String reportType, String reporterName) {
        System.out.println("EmailService: sendReportApprovedEmail called");
        System.out.println("EmailService: toEmail = " + toEmail);
        System.out.println("EmailService: userName = " + userName);
        System.out.println("EmailService: adminNotes = " + adminNotes);
        System.out.println("EmailService: reportType = " + reportType);
        System.out.println("EmailService: reporterName = " + reporterName);
        
        String subject = "Résolution de signalement - Plateforme Onboarding";
        
        String body = "<html>"
                + "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                + "<div style='background-color: #f8d7da; padding: 20px; border-radius: 8px; border: 2px solid #dc3545;'>"
                + "<h2 style='color: #721c24; text-align: center;'>✅ Signalement résolu</h2>"
                + "<p>Bonjour " + userName + ",</p>"
                + "<p>Nous vous informons que le signalement concernant votre compte a été examiné et résolu par notre équipe d'administration.</p>"
                + "<div style='background-color: #fff; padding: 15px; border-radius:5px; margin: 20px 0;'>"
                + "<p><strong>Type de signalement :</strong> " + reportType + "</p>"
                + "<p><strong>Signalé par :</strong> " + reporterName + "</p>"
                + "<p><strong>Statut :</strong> Approuvé</p>"
                + "</div>"
                + "<p><strong>Message de l'administrateur :</strong></p>"
                + "<div style='background-color: #f8f9fa; padding: 15px; border-left: 4px solid #dc3545; margin: 15px 0;'>"
                + "<p style='margin: 0;'>" + (adminNotes != null ? adminNotes : "Aucun message supplémentaire.") + "</p>"
                + "</div>"
                + "<p style='color: #721c24; font-size: 14px;'>"
                + "Nous sommes heureux de vous informer que ce problème a été résolu en votre faveur. Vous pouvez continuer à utiliser la plateforme normalement."
                + "</p>"
                + "<hr style='border: none; border-top: 1px solid #dee2e6; margin: 20px 0;'>"
                + "<p style='font-size: 12px; color: #6c757d; text-align: center;'>"
                + "© 2024 Plateforme Onboarding. Tous droits réservés."
                + "</p>"
                + "</div>"
                + "</body>"
                + "</html>";
        
        final String fromParam = from;
        final String toParam = toEmail;
        final String subjectParam = subject;
        final String bodyParam = body;
        
        // Vérifications explicites pour garantir la non-nullité
        if (fromParam == null) {
            throw new IllegalArgumentException("L'adresse d'expéditeur ne peut pas être null");
        }
        if (toParam == null) {
            throw new IllegalArgumentException("L'adresse de destinataire ne peut pas être null");
        }
        
        try {
            System.out.println("EmailService: Attempting to send email...");
            mailSender.send(mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setFrom(fromParam);
                helper.setTo(toParam);
                helper.setSubject(subjectParam);
                helper.setText(bodyParam, true); // true pour HTML
            });
            System.out.println("EmailService: Email sent successfully to " + toParam);
        } catch (Exception e) {
            System.err.println("EmailService: Error sending email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email de résolution: " + e.getMessage(), e);
        }
    }
}

package com.onboarding.service;

import com.onboarding.dto.AssignmentDTO;
import com.onboarding.dto.ChecklistDTO;
import com.onboarding.entity.Assignment;
import com.onboarding.entity.Checklist;
import com.onboarding.entity.Document;
import com.onboarding.entity.Etape;
import com.onboarding.entity.Evaluation;
import com.onboarding.entity.OnboardingParcours;
import com.onboarding.entity.StatutAssignment;
import com.onboarding.entity.StatutChecklist;
import com.onboarding.entity.User;
import com.onboarding.repository.AssignmentRepository;
import com.onboarding.repository.ChecklistRepository;
import com.onboarding.repository.DocumentRepository;
import com.onboarding.repository.EtapeRepository;
import com.onboarding.repository.EvaluationRepository;
import com.onboarding.repository.OnboardingParcoursRepository;
import com.onboarding.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OnboardingParcoursRepository parcoursRepository;
    
    @Autowired
    private ChecklistRepository checklistRepository;
    
    @Autowired
    private EtapeRepository etapeRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private DocumentRepository documentRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    // Récupérer toutes les assignations
    public List<AssignmentDTO> getAllAssignments() {
        return assignmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer les assignations pour un manager spécifique (seulement ses collaborateurs)
    public List<AssignmentDTO> getAssignmentsByManager(Long managerId) {
        System.out.println("DEBUG: getAssignmentsByManager called for manager ID: " + managerId);
        
        // Get the manager with their managed employees properly loaded
        User manager = userRepository.findByIdWithManagedEmployees(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        
        System.out.println("DEBUG: Manager found: " + manager.getNom() + " " + manager.getPrenom());
        
        // Récupérer tous les collaborateurs gérés par ce manager
        List<User> managedEmployees = manager.getManagedEmployees();
        System.out.println("DEBUG: Managed employees from relationship: " + (managedEmployees != null ? managedEmployees.size() : 0));
        
        if (managedEmployees == null || managedEmployees.isEmpty()) {
            // Fallback: try to find employees by manager_id directly
            managedEmployees = userRepository.findByManagerId(managerId);
            System.out.println("DEBUG: Managed employees from direct query: " + managedEmployees.size());
            
            if (managedEmployees.isEmpty()) {
                System.out.println("DEBUG: No managed employees found for manager " + managerId);
                return List.of();
            }
        }
        
        // Debug: Print managed employee details
        for (User emp : managedEmployees) {
            System.out.println("DEBUG: Managed employee in AssignmentService: " + emp.getId() + " (" + emp.getNom() + " " + emp.getPrenom() + "), manager: " + (emp.getManager() != null ? emp.getManager().getId() : "null"));
        }
        
        List<Long> managedUserIds = managedEmployees.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        System.out.println("DEBUG: Managed employee IDs: " + managedUserIds);
        
        // Récupérer les assignations de ces collaborateurs
        List<Assignment> assignments = assignmentRepository.findByUserIdIn(managedUserIds);
        System.out.println("DEBUG: Found " + assignments.size() + " assignments for managed employees");
        
        for (Assignment assignment : assignments) {
            System.out.println("DEBUG: Assignment in AssignmentService: " + assignment.getId() + " for user: " + assignment.getUser().getId() + " (" + assignment.getUser().getNom() + " " + assignment.getUser().getPrenom() + ")");
        }
        
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer une assignation par ID
    public Optional<AssignmentDTO> getAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    // Récupérer les assignations par utilisateur
    public List<AssignmentDTO> getAssignmentsByUserId(Long userId) {
        return assignmentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer les assignations par parcours
    public List<AssignmentDTO> getAssignmentsByParcoursId(Long parcoursId) {
        return assignmentRepository.findByParcoursId(parcoursId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Créer une nouvelle assignation
    public AssignmentDTO createAssignment(AssignmentDTO assignmentDTO, String assignePar) {
        System.out.println("Début création assignation - User ID: " + assignmentDTO.getUserId() + ", Parcours ID: " + assignmentDTO.getParcoursId());
        
        // Vérifier si l'utilisateur existe
        Optional<User> userOpt = userRepository.findById(assignmentDTO.getUserId());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + assignmentDTO.getUserId());
        }
        
        // Vérifier si le parcours existe
        Optional<OnboardingParcours> parcoursOpt = parcoursRepository.findById(assignmentDTO.getParcoursId());
        if (parcoursOpt.isEmpty()) {
            throw new RuntimeException("Parcours non trouvé avec l'ID: " + assignmentDTO.getParcoursId());
        }
        
        // Vérifier si l'assignation existe déjà
        Optional<Assignment> existingAssignment = assignmentRepository
                .findByUserIdAndParcoursId(assignmentDTO.getUserId(), assignmentDTO.getParcoursId());
        if (existingAssignment.isPresent()) {
            throw new RuntimeException("Cet utilisateur est déjà assigné à ce parcours");
        }
        
        System.out.println("Création de l'entité assignment...");
        Assignment assignment = convertToEntity(assignmentDTO);
        assignment.setAssignePar(assignePar);
        
        System.out.println("Assignment créé: " + assignment.getUser().getEmail() + " -> " + assignment.getParcours().getNom());
        
        // Calculer automatiquement la date de fin prévisionnelle
        calculerDateFinPrevisionnelle(assignment);
        
        // Mettre à jour automatiquement le statut en fonction des dates
        assignment.updateStatusBasedOnDates();
        
        System.out.println("Sauvegarde de l'assignment...");
        Assignment savedAssignment = assignmentRepository.save(assignment);
        System.out.println("Assignment sauvegardé avec ID: " + savedAssignment.getId());
        
        // Si l'assignation est créée directement en retard, envoyer les notifications
        System.out.println("DEBUG: Assignment final status: " + savedAssignment.getStatut());
        System.out.println("DEBUG: Date début: " + savedAssignment.getDateDebut());
        System.out.println("DEBUG: Date fin prévisionnelle: " + savedAssignment.getDateFinPrevisionnelle());
        System.out.println("DEBUG: Today: " + java.time.LocalDate.now());
        
        if (savedAssignment.getStatut() == StatutAssignment.EN_RETARD) {
            System.out.println("Assignment créé directement en retard - envoi des notifications");
            notificationService.sendOverdueAssignmentNotification(savedAssignment, savedAssignment.getUser());
        } else {
            System.out.println("Assignment n'est pas en retard, statut: " + savedAssignment.getStatut());
        }
        
        // Générer automatiquement la checklist (après sauvegarde pour avoir l'ID)
        genererChecklist(savedAssignment);
        
        // Envoyer la notification au collaborateur
        envoyerNotificationCollaborateur(savedAssignment);
        
        return convertToDTO(savedAssignment);
    }
    
    // Mettre à jour une assignation
    public AssignmentDTO updateAssignment(Long id, AssignmentDTO assignmentDTO) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) {
            throw new RuntimeException("Assignation non trouvée avec l'ID: " + id);
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Mettre à jour les champs
        assignment.setDateDebut(assignmentDTO.getDateDebut());
        assignment.setDateFinReelle(assignmentDTO.getDateFinReelle());
        assignment.setPourcentageAvancement(assignmentDTO.getPourcentageAvancement());
        assignment.setDateModification(LocalDate.now());
        
        // Only allow manual status updates for admin-controlled statuses
        if (assignmentDTO.getStatut() != null) {
            StatutAssignment requestedStatut = StatutAssignment.valueOf(assignmentDTO.getStatut());
            if (requestedStatut == StatutAssignment.EN_PAUSE || requestedStatut == StatutAssignment.ANNULE) {
                // Check admin privileges
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATEUR"));
                
                if (!isAdmin) {
                    throw new RuntimeException("Seuls les administrateurs peuvent définir le statut '" + requestedStatut.getDisplayName() + "'");
                }
                assignment.setStatut(requestedStatut);
            } else if (requestedStatut == StatutAssignment.TERMINE) {
                // Allow TERMINE status (completion)
                assignment.setStatut(requestedStatut);
                assignment.setDateFinReelle(LocalDate.now());
            }
            // Don't allow manual setting of EN_ATTENTE, EN_COURS, EN_RETARD - they are automatic
        }
        
        // Recalculer la date de fin prévisionnelle si nécessaire
        if (assignmentDTO.getDateFinPrevisionnelle() != null) {
            assignment.setDateFinPrevisionnelle(assignmentDTO.getDateFinPrevisionnelle());
        } else {
            calculerDateFinPrevisionnelle(assignment);
        }
        
        // Mettre à jour automatiquement le statut en fonction des dates
        assignment.updateStatusBasedOnDates();
        
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return convertToDTO(updatedAssignment);
    }
    
    // Récupérer les assignations de l'utilisateur connecté
    public List<AssignmentDTO> getAssignmentsForCurrentUser() {
        try {
            // Récupérer l'email de l'utilisateur connecté depuis le contexte de sécurité
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail;
            
            if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
                userEmail = authentication.getName();
            } else {
                // Use default user email when no authentication context
                userEmail = "collab@gmail.com";
            }
            
            // Trouver l'utilisateur par email
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userEmail));
            
            // Récupérer les assignations de cet utilisateur
            List<Assignment> assignments = assignmentRepository.findByUserId(user.getId());
            
            return assignments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("DEBUG: Error in getAssignmentsForCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Mettre à jour le statut d'une assignation (réservé aux admins pour EN_PAUSE et ANNULE uniquement)
    public AssignmentDTO updateStatutAssignment(Long id, String statut) {
        System.out.println("Tentative de mise à jour statut - ID: " + id + ", Statut reçu: '" + statut + "'");
        
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) {
            throw new RuntimeException("Assignation non trouvée avec l'ID: " + id);
        }
        
        Assignment assignment = assignmentOpt.get();
        
        try {
            StatutAssignment newStatut = StatutAssignment.valueOf(statut);
            
            // Check if the requested status requires admin privileges
            if (newStatut == StatutAssignment.EN_PAUSE || newStatut == StatutAssignment.ANNULE) {
                // Vérifier si l'utilisateur actuel est un admin (via le contexte de sécurité)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATEUR"));
                
                if (!isAdmin) {
                    throw new RuntimeException("Seuls les administrateurs peuvent définir le statut '" + newStatut.getDisplayName() + "'");
                }
            } else if (newStatut == StatutAssignment.EN_ATTENTE || newStatut == StatutAssignment.EN_COURS || 
                      newStatut == StatutAssignment.EN_RETARD) {
                // These statuses are managed automatically - admins cannot set them manually
                throw new RuntimeException("Le statut '" + newStatut.getDisplayName() + "' est géré automatiquement en fonction des dates. Seuls les statuts 'En pause' et 'Annulé' peuvent être modifiés manuellement par les administrateurs.");
            }
            
            assignment.setStatut(newStatut);
            
            // Si le statut passe à TERMINE, mettre la date de fin réelle et envoyer une notification
            if (StatutAssignment.TERMINE.equals(newStatut)) {
                System.out.println("DEBUG: Assignment status changing to TERMINE in updateStatutAssignment - sending notification");
                assignment.setDateFinReelle(LocalDate.now());
                
                // Envoyer une notification de complétion d'assignation au manager
                User collaborateur = assignment.getUser();
                if (collaborateur != null) {
                    System.out.println("DEBUG: Calling sendAssignmentCompletionNotification for assignment: " + assignment.getId());
                    notificationService.sendAssignmentCompletionNotification(assignment, collaborateur);
                }
            }
            
            Assignment updatedAssignment = assignmentRepository.save(assignment);
            return convertToDTO(updatedAssignment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide: " + statut + ". Valeurs valides: " + java.util.Arrays.toString(StatutAssignment.values()));
        }
    }
    
    // Mettre à jour l'avancement
    public AssignmentDTO updateAvancement(Long id, Integer pourcentage) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) {
            throw new RuntimeException("Assignation non trouvée avec l'ID: " + id);
        }
        
        Assignment assignment = assignmentOpt.get();
        assignment.setPourcentageAvancement(Math.min(100, Math.max(0, pourcentage)));
        assignment.setDateModification(LocalDate.now());
        
        // Si 100%, marquer comme terminé
        if (assignment.getPourcentageAvancement() >= 100) {
            assignment.setStatut(StatutAssignment.TERMINE);
            assignment.setDateFinReelle(LocalDate.now());
        }

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        if (updatedAssignment.getStatut() == StatutAssignment.TERMINE && updatedAssignment.getUser() != null) {
            notificationService.sendAssignmentCompletionNotification(updatedAssignment, updatedAssignment.getUser());
            notificationService.sendParcoursCompleteNotification(updatedAssignment, updatedAssignment.getUser());
        }
        return convertToDTO(updatedAssignment);
    }
    
    // Récupérer les assignations en retard
    public List<AssignmentDTO> getAssignmentsEnRetard() {
        return assignmentRepository.findAssignmentsEnRetard(LocalDate.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer les assignations en retard pour un manager spécifique
    public List<AssignmentDTO> getAssignmentsEnRetardByManager(Long managerId) {
        // Get the manager with their managed employees properly loaded
        User manager = userRepository.findByIdWithManagedEmployees(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        
        List<User> managedEmployees = manager.getManagedEmployees();
        if (managedEmployees == null || managedEmployees.isEmpty()) {
            // Fallback: try to find employees by manager_id directly
            managedEmployees = userRepository.findByManagerId(managerId);
            if (managedEmployees.isEmpty()) {
                return List.of();
            }
        }
        
        List<Long> managedUserIds = managedEmployees.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        return assignmentRepository.findAssignmentsEnRetard(LocalDate.now()).stream()
                .filter(a -> managedUserIds.contains(a.getUser().getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer les assignations proches de l'échéance
    public List<AssignmentDTO> getAssignmentsProchesEcheance() {
        LocalDate endDate = LocalDate.now().plusDays(7);
        return assignmentRepository.findAssignmentsProchesEcheance(LocalDate.now(), endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Récupérer les assignations proches de l'échéance pour un manager spécifique
    public List<AssignmentDTO> getAssignmentsProchesEcheanceByManager(Long managerId) {
        // Get the manager with their managed employees properly loaded
        User manager = userRepository.findByIdWithManagedEmployees(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        
        List<User> managedEmployees = manager.getManagedEmployees();
        if (managedEmployees == null || managedEmployees.isEmpty()) {
            // Fallback: try to find employees by manager_id directly
            managedEmployees = userRepository.findByManagerId(managerId);
            if (managedEmployees.isEmpty()) {
                return List.of();
            }
        }
        
        List<Long> managedUserIds = managedEmployees.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        LocalDate endDate = LocalDate.now().plusDays(7);
        return assignmentRepository.findAssignmentsProchesEcheance(LocalDate.now(), endDate).stream()
                .filter(a -> managedUserIds.contains(a.getUser().getId()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Vérifier si une assignation existe
    public boolean existsById(Long id) {
        return assignmentRepository.existsById(id);
    }
    
    // Méthodes privées
    
    private void calculerDateFinPrevisionnelle(Assignment assignment) {
        if (assignment.getParcours() != null && assignment.getDateDebut() != null) {
            Integer dureeEstimee = assignment.getParcours().getDureeGlobaleEstimee();
            if (dureeEstimee != null) {
                assignment.setDateFinPrevisionnelle(assignment.getDateDebut().plusDays(dureeEstimee));
            }
        }
    }
    
    private void genererChecklist(Assignment assignment) {
        System.out.println("Génération automatique de la checklist pour l'assignation ID: " + assignment.getId());
        
        if (assignment.getParcours() == null) {
            System.out.println("Aucun parcours associé à l'assignation, génération de checklist annulée");
            return;
        }
        
        // Récupérer les étapes du parcours
        List<Etape> etapes = etapeRepository.findByParcours_IdOrderByOrdreExecution(assignment.getParcours().getId());
        
        if (etapes.isEmpty()) {
            System.out.println("Aucune étape trouvée pour le parcours ID: " + assignment.getParcours().getId());
            return;
        }
        
        System.out.println("Trouvé " + etapes.size() + " étapes pour générer la checklist");
        
        // Générer une entrée de checklist pour chaque étape
        for (Etape etape : etapes) {
            Checklist checklist = new Checklist();
            checklist.setAssignment(assignment);
            checklist.setEtape(etape); // IMPORTANT: Link to the actual etape entity
            checklist.setTitre(etape.getNom());
            checklist.setDescription("Étape: " + etape.getNom() + " (" + etape.getType() + ")");
            checklist.setStatut(StatutChecklist.EN_ATTENTE);
            checklist.setObligatoire(true); // Toutes les étapes sont obligatoires par défaut
            checklist.setRequiertDocument(etape.getRequiertDocument() != null ? etape.getRequiertDocument() : false);
            checklist.setOrdre(etape.getOrdreExecution() != null ? etape.getOrdreExecution() : 1);
            checklist.setCreePar("System Auto");
            
            Checklist savedChecklist = checklistRepository.save(checklist);
            System.out.println("Checklist créée: " + savedChecklist.getTitre() + " (ID: " + savedChecklist.getId() + ") - Étape ID: " + etape.getId());
        }
        
        System.out.println("Génération de checklist terminée pour l'assignation ID: " + assignment.getId());
    }
    
    private void envoyerNotificationCollaborateur(Assignment assignment) {
        notificationService.sendAssignmentAssignedNotification(assignment);
    }
    
    // Conversion entre Entity et DTO
    private AssignmentDTO convertToDTO(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(assignment.getId());
        dto.setUserId(assignment.getUser() != null ? assignment.getUser().getId() : null);
        dto.setParcoursId(assignment.getParcours() != null ? assignment.getParcours().getId() : null);
        dto.setUserName(assignment.getUser() != null ? assignment.getUser().getNom() : null);
        dto.setUserPrenom(assignment.getUser() != null ? assignment.getUser().getPrenom() : null);
        dto.setUserEmail(assignment.getUser() != null ? assignment.getUser().getEmail() : null);
        dto.setParcoursNom(assignment.getParcours() != null ? assignment.getParcours().getNom() : null);
        dto.setDateDebut(assignment.getDateDebut());
        dto.setDateFinPrevisionnelle(assignment.getDateFinPrevisionnelle());
        dto.setDateFinReelle(assignment.getDateFinReelle());
        dto.setStatut(assignment.getStatut() != null ? assignment.getStatut().name() : null);
        dto.setPourcentageAvancement(assignment.getPourcentageAvancement());
        dto.setDateCreation(assignment.getDateCreation());
        dto.setDateModification(assignment.getDateModification());
        dto.setAssignePar(assignment.getAssignePar());
        return dto;
    }
    
    private Assignment convertToEntity(AssignmentDTO dto) {
        Assignment assignment = new Assignment();
        assignment.setId(dto.getId());
        assignment.setDateDebut(dto.getDateDebut());
        assignment.setDateFinPrevisionnelle(dto.getDateFinPrevisionnelle());
        assignment.setDateFinReelle(dto.getDateFinReelle());
        assignment.setPourcentageAvancement(dto.getPourcentageAvancement());
        assignment.setDateCreation(dto.getDateCreation() != null ? dto.getDateCreation() : java.time.LocalDate.now());
        assignment.setDateModification(dto.getDateModification());
        assignment.setAssignePar(dto.getAssignePar());
        
        // Définir les relations
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + dto.getUserId()));
            assignment.setUser(user);
        }
        
        if (dto.getParcoursId() != null) {
            OnboardingParcours parcours = parcoursRepository.findById(dto.getParcoursId())
                    .orElseThrow(() -> new RuntimeException("Parcours non trouvé: " + dto.getParcoursId()));
            assignment.setParcours(parcours);
        }
        
        if (dto.getStatut() != null) {
            assignment.setStatut(StatutAssignment.valueOf(dto.getStatut()));
        }
        
        return assignment;
    }
    
    // Supprimer une assignation et ses checklists associées
    public void deleteAssignment(Long id) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isEmpty()) {
            throw new RuntimeException("Assignation non trouvée avec l'ID: " + id);
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // D'abord supprimer tous les évaluations associées aux checklists de cette assignation
        try {
            // Récupérer toutes les checklists associées à cette assignation
            List<Checklist> checklists = checklistRepository.findByAssignmentId(id);
            
            // Supprimer les évaluations pour chaque checklist
            int totalEvaluationsDeleted = 0;
            for (Checklist checklist : checklists) {
                List<Evaluation> evaluations = evaluationRepository.findByChecklist(checklist);
                if (!evaluations.isEmpty()) {
                    System.out.println("Suppression de " + evaluations.size() + " évaluations pour la checklist ID: " + checklist.getId());
                    evaluationRepository.deleteAll(evaluations);
                    totalEvaluationsDeleted += evaluations.size();
                }
            }
            
            if (totalEvaluationsDeleted > 0) {
                System.out.println("Total de " + totalEvaluationsDeleted + " évaluations supprimées pour l'assignation ID: " + id);
            }
            
            // Supprimer les documents pour chaque checklist
            int totalDocumentsDeleted = 0;
            for (Checklist checklist : checklists) {
                if (checklist.getEtape() != null) {
                    List<Document> documents = documentRepository.findByEtapeId(checklist.getEtape().getId());
                    if (!documents.isEmpty()) {
                        System.out.println("Suppression de " + documents.size() + " documents pour l'étape ID: " + checklist.getEtape().getId());
                        documentRepository.deleteAll(documents);
                        totalDocumentsDeleted += documents.size();
                    }
                }
            }
            
            if (totalDocumentsDeleted > 0) {
                System.out.println("Total de " + totalDocumentsDeleted + " documents supprimés pour l'assignation ID: " + id);
            }
            
            // Ensuite supprimer les checklists
            if (!checklists.isEmpty()) {
                System.out.println("Suppression de " + checklists.size() + " checklists pour l'assignation ID: " + id);
                checklistRepository.deleteAll(checklists);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression des évaluations/documents/checklists: " + e.getMessage());
            e.printStackTrace();
            // Continuer avec la suppression de l'assignation même si les évaluations/documents/checklists échouent
        }
        
        // Ensuite supprimer l'assignation
        System.out.println("Suppression de l'assignation ID: " + id);
        assignmentRepository.delete(assignment);
        System.out.println("Assignation supprimée avec succès");
    }
    
    // Méthodes pour la gestion des checklists
    // Récupérer toutes les checklists pour une assignation
    public List<ChecklistDTO> getChecklistsByAssignmentId(Long assignmentId) {
        return checklistRepository.findByAssignmentId(assignmentId).stream()
                .map(this::convertChecklistToDTO)
                .collect(Collectors.toList());
    }
    
    // Vérifier si une checklist est accessible (déverrouillée) selon l'ordre et le statut
    public boolean isChecklistUnlocked(Long checklistId) {
        Optional<Checklist> currentChecklistOpt = checklistRepository.findById(checklistId);
        if (currentChecklistOpt.isEmpty()) {
            return false;
        }
        
        Checklist currentChecklist = currentChecklistOpt.get();
        Long assignmentId = currentChecklist.getAssignment().getId();
        Integer currentOrdre = currentChecklist.getOrdre();
        
        // Récupérer l'utilisateur actuel et son rôle
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATEUR"));
        
        // Les admins peuvent tout modifier
        if (isAdmin) {
            return true;
        }
        
        // Récupérer l'assignation pour vérifier son statut
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment != null && assignment.getStatut() == StatutAssignment.EN_ATTENTE) {
            // Si l'assignation est en attente, toutes les checklists sont bloquées
            return false;
        }
        
        // Si la checklist actuelle est déjà terminée, elle est verrouillée pour les collaborateurs
        if (currentChecklist.getStatut() == StatutChecklist.TERMINE) {
            return false;
        }
        
        // Si c'est la première tâche (ordre 1), elle est déverrouillée seulement si pas terminée
        if (currentOrdre == null || currentOrdre <= 1) {
            return true;
        }
        
        // Récupérer toutes les checklists de cette assignation
        List<Checklist> allChecklists = checklistRepository.findByAssignmentId(assignmentId);
        
        // Trouver la checklist précédente (ordre - 1)
        Checklist previousChecklist = allChecklists.stream()
                .filter(c -> c.getOrdre() != null && c.getOrdre().equals(currentOrdre - 1))
                .findFirst()
                .orElse(null);
        
        // Si pas de checklist précédente, déverrouiller
        if (previousChecklist == null) {
            return true;
        }
        
        // La checklist est déverrouillée si la précédente est terminée ou sautée
        return previousChecklist.getStatut() == StatutChecklist.TERMINE || 
               previousChecklist.getStatut() == StatutChecklist.SAUTE;
    }
    
    // Calculer automatiquement la progression basée sur les checklists
    public void updateProgressionBasedOnChecklists(Long assignmentId) {
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
        if (assignmentOpt.isEmpty()) {
            return;
        }
        
        Assignment assignment = assignmentOpt.get();
        
        // Récupérer toutes les checklists pour cette assignation
        List<Checklist> checklists = checklistRepository.findByAssignmentId(assignmentId);
        
        if (checklists.isEmpty()) {
            return;
        }
        
        // Calculer le nombre de tâches terminées
        long completedTasks = checklists.stream()
                .filter(c -> c.getStatut() == StatutChecklist.TERMINE)
                .count();
        
        // Calculer le pourcentage de progression
        int progression = (int) ((completedTasks * 100) / checklists.size());
        
        // Mettre à jour la progression
        assignment.setPourcentageAvancement(progression);
        
        // Si toutes les checklists sont terminées, marquer le parcours comme terminé
        if (completedTasks == checklists.size()) {
            assignment.setStatut(StatutAssignment.TERMINE);
            assignment.setDateFinReelle(LocalDate.now());
            System.out.println("Parcours terminé - toutes les checklists sont complétées pour l'assignation " + assignmentId);
            
            User collaborateur = assignment.getUser();
            if (collaborateur != null) {
                notificationService.sendAssignmentCompletionNotification(assignment, collaborateur);
                notificationService.sendParcoursCompleteNotification(assignment, collaborateur);
            }
        } else {
            // Utiliser la méthode automatique de l'entité pour gérer les statuts basés sur les dates
            assignment.updateStatusBasedOnDates();
        }
        
        assignmentRepository.save(assignment);
        System.out.println("Progression et statut mis à jour pour l'assignation " + assignmentId + ": " + progression + "%, statut: " + assignment.getStatut());
    }
    
    // Vérifier et mettre à jour automatiquement les statuts basés sur les dates
    public void checkAndUpdateOverdueAssignments() {
        // Récupérer toutes les assignations qui pourraient avoir besoin d'une mise à jour de statut
        List<Assignment> allAssignments = assignmentRepository.findAll();
        
        for (Assignment assignment : allAssignments) {
            StatutAssignment oldStatut = assignment.getStatut();
            
            // Utiliser la méthode automatique de l'entité pour mettre à jour le statut
            assignment.updateStatusBasedOnDates();
            
            // Sauvegarder uniquement si le statut a changé
            if (!oldStatut.equals(assignment.getStatut())) {
                assignmentRepository.save(assignment);
                System.out.println("Statut mis à jour automatiquement pour l'assignation " + assignment.getId() + 
                                 ": " + oldStatut.getDisplayName() + " -> " + assignment.getStatut().getDisplayName());
                
                // Si le statut passe à EN_RETARD, envoyer les notifications
                if (assignment.getStatut() == StatutAssignment.EN_RETARD) {
                    User collaborateur = assignment.getUser();
                    if (collaborateur != null) {
                        System.out.println("Envoi des notifications de retard pour l'assignation " + assignment.getId());
                        notificationService.sendOverdueAssignmentNotification(assignment, collaborateur);
                    }
                }
            }
        }
    }
    
    // Mettre à jour le statut d'une checklist
    public ChecklistDTO updateChecklistStatut(Long checklistId, String statut) {
        Optional<Checklist> checklistOpt = checklistRepository.findById(checklistId);
        if (checklistOpt.isEmpty()) {
            throw new RuntimeException("Checklist non trouvée avec l'ID: " + checklistId);
        }
        
        Checklist checklist = checklistOpt.get();
        
        // Vérifier si l'assignation est en attente
        Assignment assignment = checklist.getAssignment();
        if (assignment != null && assignment.getStatut() == StatutAssignment.EN_ATTENTE) {
            throw new RuntimeException("Impossible de modifier la checklist tant que l'assignation est en attente. Veuillez attendre la date de début du parcours.");
        }
        
        try {
            StatutChecklist newStatut = StatutChecklist.valueOf(statut);
            System.out.println("DEBUG: Updating checklist " + checklistId + " status to: " + newStatut);
            
            // Validation: Si le statut passe à TERMINE et que la checklist requiert un document
            if (StatutChecklist.TERMINE.equals(newStatut) && checklist.getRequiertDocument()) {
                System.out.println("DEBUG: Checklist requires document validation");
                System.out.println("DEBUG: Checklist ID: " + checklist.getId());
                System.out.println("DEBUG: Checklist requiertDocument: " + checklist.getRequiertDocument());
                
                // Vérifier si l'étape associée existe
                if (checklist.getEtape() == null) {
                    System.out.println("DEBUG: Checklist has no associated étape");
                    throw new RuntimeException("📋 Cette étape requiert un document obligatoire mais aucune étape n'est associée à cette checklist. Veuillez contacter votre administrateur.");
                }
                
                System.out.println("DEBUG: Found associated étape ID: " + checklist.getEtape().getId());
                
                // Vérifier si un document existe pour cette étape
                List<Document> documents = documentRepository.findByEtapeId(checklist.getEtape().getId());
                System.out.println("DEBUG: Found " + documents.size() + " documents for étape ID: " + checklist.getEtape().getId());
                
                if (documents.isEmpty()) {
                    throw new RuntimeException("📋 Cette étape requiert un document obligatoire. Veuillez d'abord télécharger le document nécessaire avant de pouvoir marquer cette checklist comme terminée.");
                }
                
                // Debug: Print document details
                for (Document doc : documents) {
                    System.out.println("DEBUG: Document - ID: " + doc.getId() + ", Filename: " + doc.getFilename() + 
                            ", IsSigned: " + doc.getIsSigned() + ", SignedByUserId: " + doc.getSignedByUserId());
                }
                
                // Vérifier si au moins un document a été signé par le manager
                User collaborateur = assignment.getUser();
                User manager = collaborateur.getManager();
                
                System.out.println("DEBUG: Collaborateur: " + collaborateur.getPrenom() + " " + collaborateur.getNom());
                System.out.println("DEBUG: Manager: " + (manager != null ? manager.getPrenom() + " " + manager.getNom() + " (ID: " + manager.getId() + ")" : "NULL"));
                
                boolean hasManagerSignedDocument = false;
                boolean hasUnsignedDocument = false;
                
                for (Document document : documents) {
                    if (document.getIsSigned() && document.getSignedByUserId() != null) {
                        System.out.println("DEBUG: Document " + document.getFilename() + " is signed by user ID: " + document.getSignedByUserId());
                        // Vérifier si le signataire est le manager du collaborateur
                        if (manager != null && document.getSignedByUserId().equals(manager.getId())) {
                            hasManagerSignedDocument = true;
                            System.out.println("DEBUG: Found document signed by manager: " + document.getFilename());
                            break;
                        } else {
                            System.out.println("DEBUG: Document signed by user " + document.getSignedByUserId() + " but manager ID is " + (manager != null ? manager.getId() : "NULL"));
                        }
                    } else {
                        hasUnsignedDocument = true;
                        System.out.println("DEBUG: Document " + document.getFilename() + " is not signed");
                    }
                }
                
                System.out.println("DEBUG: hasManagerSignedDocument: " + hasManagerSignedDocument);
                System.out.println("DEBUG: hasUnsignedDocument: " + hasUnsignedDocument);
                
                if (!hasManagerSignedDocument) {
                    if (hasUnsignedDocument) {
                        throw new RuntimeException("✍️ Un document a été téléchargé mais il doit d'abord être validé et signé par votre manager (" + 
                                (manager != null ? manager.getPrenom() + " " + manager.getNom() : "votre manager") + 
                                ") avant de pouvoir marquer cette checklist comme terminée.");
                    } else {
                        throw new RuntimeException("✍️ Le document requis a été téléchargé mais doit être signé par votre manager (" + 
                                (manager != null ? manager.getPrenom() + " " + manager.getNom() : "votre manager") + 
                                ") avant de pouvoir marquer cette checklist comme terminée.");
                    }
                }
                
                System.out.println("DEBUG: Document validation passed - allowing checklist completion");
            }
            
            checklist.setStatut(newStatut);
            
            if (StatutChecklist.TERMINE.equals(newStatut)) {
                checklist.setDateRealisation(LocalDate.now());
            }

            Checklist updatedChecklist = checklistRepository.save(checklist);

            if (StatutChecklist.TERMINE.equals(newStatut) && assignment.getUser() != null) {
                notificationService.sendEtapeCompleteNotification(updatedChecklist, assignment.getUser());
            }
            System.out.println("DEBUG: Checklist saved with new status: " + updatedChecklist.getStatut());
            
            // Si le statut passe à BLOQUE, envoyer une notification au manager
            if (StatutChecklist.BLOQUE.equals(newStatut)) {
                System.out.println("DEBUG: Checklist is BLOQUE - checking for manager");
                User collaborateur = assignment.getUser();
                System.out.println("DEBUG: Collaborateur: " + (collaborateur != null ? collaborateur.getPrenom() + " " + collaborateur.getNom() : "null"));
                if (collaborateur != null && collaborateur.getManager() != null) {
                    System.out.println("DEBUG: Manager found: " + collaborateur.getManager().getPrenom() + " " + collaborateur.getManager().getNom());
                    System.out.println("Checklist bloquée - envoi de notification au manager");
                    notificationService.sendChecklistBloqueeNotification(updatedChecklist, collaborateur);
                } else {
                    System.out.println("DEBUG: No manager found for collaborateur");
                }
            } else {
                System.out.println("DEBUG: Checklist status is not BLOQUE, skipping notification");
            }
            
            // Mettre à jour automatiquement la progression de l'assignation
            updateProgressionBasedOnChecklists(checklist.getAssignment().getId());
            
            return convertChecklistToDTO(updatedChecklist);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide: " + statut + ". Valeurs valides: " + java.util.Arrays.toString(StatutChecklist.values()));
        }
    }
    
    // Conversion entre Entity et DTO pour Checklist
    private ChecklistDTO convertChecklistToDTO(Checklist checklist) {
        ChecklistDTO dto = new ChecklistDTO();
        dto.setId(checklist.getId());
        dto.setAssignmentId(checklist.getAssignment() != null ? checklist.getAssignment().getId() : null);
        dto.setTitre(checklist.getTitre());
        dto.setDescription(checklist.getDescription());
        dto.setStatut(checklist.getStatut() != null ? checklist.getStatut().name() : null);
        dto.setOrdre(checklist.getOrdre());
        dto.setObligatoire(checklist.getObligatoire());
        dto.setRequiertDocument(checklist.getRequiertDocument());
        dto.setDateCreation(checklist.getDateCreation());
        dto.setDateRealisation(checklist.getDateRealisation());
        dto.setCreePar(checklist.getCreePar());
        dto.setEtapeId(checklist.getEtape() != null ? checklist.getEtape().getId() : null);
        
        // Déterminer si la checklist est déverrouillée
        boolean isUnlocked = isChecklistUnlocked(checklist.getId());
        dto.setUnlocked(isUnlocked);
        
        // Déterminer si la checklist est terminée et donc verrouillée
        boolean isLockedCompleted = checklist.getStatut() == StatutChecklist.TERMINE && !isUnlocked;
        dto.setLockedCompleted(isLockedCompleted);
        
        return dto;
    }
}

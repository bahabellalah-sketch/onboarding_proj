export type NotificationTypeKey =
  | 'ETAPE_COMPLETE'
  | 'PARCOURS_COMPLETE'
  | 'ASSIGNMENT_COMPLETE'
  | 'ASSIGNMENT_ASSIGNED'
  | 'ASSIGNMENT_OVERDUE'
  | 'CHECKLIST_COMPLETE'
  | 'CHECKLIST_BLOQUEE'
  | 'USER_CREATED'
  | 'USER_UPDATED'
  | 'USER_DELETED'
  | 'SYSTEM_ALERT'
  | 'DEADLINE_REMINDER'
  | 'DOCUMENT_UPLOADED'
  | 'DOCUMENT_MISSING'
  | 'DOCUMENT_SIGNED'
  | 'EVALUATION_RECEIVED'
  | 'MANAGER_NOTIFICATION'
  | 'COLLABORATEUR_NOTIFICATION'
  | 'ADMIN_NOTIFICATION';

const ICONS: Record<string, string> = {
  ETAPE_COMPLETE: '✓',
  PARCOURS_COMPLETE: '🎓',
  ASSIGNMENT_COMPLETE: '✅',
  ASSIGNMENT_ASSIGNED: '📋',
  ASSIGNMENT_OVERDUE: '⚠️',
  CHECKLIST_COMPLETE: '☑️',
  CHECKLIST_BLOQUEE: '🚫',
  USER_CREATED: '👤',
  USER_UPDATED: '✏️',
  USER_DELETED: '🗑️',
  SYSTEM_ALERT: '🔔',
  DEADLINE_REMINDER: '⏰',
  DOCUMENT_UPLOADED: '📄',
  DOCUMENT_MISSING: '❌',
  DOCUMENT_SIGNED: '✍️',
  EVALUATION_RECEIVED: '⭐',
  MANAGER_NOTIFICATION: '👨‍💼',
  COLLABORATEUR_NOTIFICATION: '👥',
  ADMIN_NOTIFICATION: '🔐',
};

const LABELS: Record<string, string> = {
  ETAPE_COMPLETE: 'Étape terminée',
  PARCOURS_COMPLETE: 'Parcours terminé',
  ASSIGNMENT_COMPLETE: 'Assignation terminée',
  ASSIGNMENT_ASSIGNED: 'Nouveau parcours',
  ASSIGNMENT_OVERDUE: 'En retard',
  CHECKLIST_COMPLETE: 'Tâche terminée',
  CHECKLIST_BLOQUEE: 'Tâche bloquée',
  USER_CREATED: 'Utilisateur créé',
  USER_UPDATED: 'Utilisateur modifié',
  USER_DELETED: 'Utilisateur supprimé',
  SYSTEM_ALERT: 'Alerte système',
  DEADLINE_REMINDER: 'Échéance proche',
  DOCUMENT_UPLOADED: 'Document déposé',
  DOCUMENT_MISSING: 'Document manquant',
  DOCUMENT_SIGNED: 'Document signé',
  EVALUATION_RECEIVED: 'Évaluation reçue',
  MANAGER_NOTIFICATION: 'Manager',
  COLLABORATEUR_NOTIFICATION: 'Collaborateur',
  ADMIN_NOTIFICATION: 'Administration',
};

export const getNotificationIcon = (type: string): string =>
  ICONS[type] ?? '📢';

export const getNotificationLabel = (type: string): string =>
  LABELS[type] ?? 'Notification';

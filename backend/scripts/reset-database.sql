-- Réinitialise toutes les données de onboarding_db (structure conservée)
-- Usage: mysql -u root onboarding_db < scripts/reset-database.sql
/*
USE onboarding_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE evaluations;
TRUNCATE TABLE document_signature_audit;
TRUNCATE TABLE documents;
TRUNCATE TABLE team_messages;
TRUNCATE TABLE notifications;
TRUNCATE TABLE user_reports;
TRUNCATE TABLE checklists;
TRUNCATE TABLE assignments;
TRUNCATE TABLE etapes;
TRUNCATE TABLE email_verification;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE user_profiles;
TRUNCATE TABLE onboarding_parcours;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Base onboarding_db vidée — prête pour de nouveaux tests.' AS message;
*/

package com.onboarding.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update n'élève pas toujours les contraintes NOT NULL.
 * Les évaluations parcours (manager / collaborateur) n'ont pas de checklist_id.
 */
@Component
public class EvaluationDatabaseMigration {

    private final JdbcTemplate jdbcTemplate;

    public EvaluationDatabaseMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrateEvaluationsTable() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE evaluations MODIFY COLUMN checklist_id BIGINT NULL"
            );
            System.out.println("EvaluationDatabaseMigration: checklist_id nullable OK");
        } catch (Exception e) {
            System.out.println("EvaluationDatabaseMigration: checklist_id — " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                    "ALTER TABLE evaluations ADD COLUMN IF NOT EXISTS assignment_id BIGINT NULL"
            );
        } catch (Exception e) {
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE evaluations ADD COLUMN assignment_id BIGINT NULL"
                );
            } catch (Exception ignored) {
                // colonne déjà présente
            }
        }

        try {
            jdbcTemplate.execute(
                    "ALTER TABLE evaluations MODIFY COLUMN assignment_id BIGINT NULL"
            );
        } catch (Exception e) {
            System.out.println("EvaluationDatabaseMigration: assignment_id — " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                    "ALTER TABLE evaluations ADD COLUMN IF NOT EXISTS evaluation_type VARCHAR(32) NOT NULL DEFAULT 'ETAPE'"
            );
        } catch (Exception e) {
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE evaluations ADD COLUMN evaluation_type VARCHAR(32) NOT NULL DEFAULT 'ETAPE'"
                );
            } catch (Exception ignored) {
                // déjà présente
            }
        }

        try {
            jdbcTemplate.execute(
                    "ALTER TABLE evaluations ADD COLUMN IF NOT EXISTS recommendation VARCHAR(32) NULL"
            );
        } catch (Exception e) {
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE evaluations ADD COLUMN recommendation VARCHAR(32) NULL"
                );
            } catch (Exception ignored) {
                // déjà présente
            }
        }

        try {
            jdbcTemplate.execute(
                    "UPDATE evaluations SET evaluation_type = 'ETAPE' WHERE evaluation_type IS NULL OR evaluation_type = ''"
            );
        } catch (Exception ignored) {
            // ignore
        }
    }
}

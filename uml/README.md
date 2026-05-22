# Diagrammes UML — Plateforme d'Onboarding

Diagrammes au format **PlantUML**, compatibles avec **StarUML** (via extension) et tout outil PlantUML.

## Fichiers

| Fichier | Description |
|---------|-------------|
| `use-case-diagram.puml` | Diagramme de cas d'utilisation (acteurs, 11 packages UC, include/extend) |
| `class-diagram.puml` | Diagramme de classes (entités JPA, énumérations, services, contrôleurs REST) |

## Visualisation

### Option 1 — En ligne (rapide)

1. Ouvrir [https://www.plantuml.com/plantuml](https://www.plantuml.com/plantuml)
2. Coller le contenu du fichier `.puml`
3. Exporter en PNG ou SVG

### Option 2 — VS Code / Cursor

1. Installer l’extension **PlantUML**
2. Ouvrir le fichier `.puml`
3. `Alt+D` pour prévisualiser

### Option 3 — StarUML

1. Installer l’extension **PlantUML** pour StarUML (Marketplace / GitHub)
2. **Tools → PlantUML → Import** (ou glisser-déposer le `.puml`)
3. Ajuster la mise en page si nécessaire
4. Exporter : **File → Export Diagram** (PNG, PDF)

### Option 4 — Ligne de commande

```bash
java -jar plantuml.jar uml/use-case-diagram.puml
java -jar plantuml.jar uml/class-diagram.puml
```

## Acteurs (cas d'utilisation)

| Acteur | Rôle métier |
|--------|-------------|
| **Administrateur** | Gestion complète, analytics, signalements, notifications système |
| **Manager** | Équipe, affectations, parcours, signature documents |
| **Collaborateur** | Parcours assignés, checklists, documents, évaluations, chat |
| **Système Email** | SMTP (reset password, vérification, bienvenue) |
| **Planificateur** | Tâches `@Scheduled` (retards, rappels) |

## Relations clés (classes)

```
User ──manager──> User (auto-référence)
OnboardingParcours ──1..*── Etape
User + Parcours ──> Assignment ──1..*── Checklist ──0..*── Evaluation
Etape ──(etapeId)── Document ──audit── DocumentSignatureAudit
```

## Conformité académique

- Notation UML 2.5
- Stéréotypes : `<<Entity>>`, `<<Service>>`, `<<RestController>>`, `<<enumeration>>`
- Multiplicités sur les associations
- Relations `<<include>>` / `<<extend>>` sur le diagramme de cas d'utilisation
- Généralisation entre acteurs (Admin → Manager → Collaborateur)

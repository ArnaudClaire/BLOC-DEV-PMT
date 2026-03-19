# Database

Ce dossier centralise les ressources liees a la base de donnees locale.

- `database.mv.db` : ancien fichier H2 local conserve pour reference.
- `migrations/V1__initial_schema_postgresql.sql` : premier schema PostgreSQL versionne du projet.
- La base principale de l'application reste PostgreSQL via `docker-compose.yml`.

## Objectif du schema versionne

Le backend fonctionne encore aujourd'hui avec `spring.jpa.hibernate.ddl-auto=update`, mais le fichier
`migrations/V1__initial_schema_postgresql.sql` fournit maintenant une version explicite et versionnee
de la structure attendue par l'application.

Ce script documente :

- les tables metier
- les cles primaires et etrangeres
- les contraintes enumerees
- les index utiles aux parcours courants

Il peut servir de base pour une future adoption de Flyway ou Liquibase.

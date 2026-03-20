# Database

Ce dossier centralise les ressources liées à la base de données locale.

- `migrations/V1__initial_schema_postgresql.sql` : premier schéma PostgreSQL versionné du projet
- la base principale de l'application reste PostgreSQL via `docker-compose.yml`

## Objectif du schéma versionné

Le backend fonctionne encore aujourd'hui avec `spring.jpa.hibernate.ddl-auto=update`, mais le fichier
`migrations/V1__initial_schema_postgresql.sql` fournit maintenant une version explicite et versionnée
de la structure attendue par l'application.

Ce script documente :

- les tables métier
- les clés primaires et étrangères
- les contraintes énumérées
- les index utiles aux parcours courants

Il peut servir de base pour une future adoption de Flyway ou Liquibase.

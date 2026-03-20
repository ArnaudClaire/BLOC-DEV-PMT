# Database

Ce dossier centralise les ressources liées à la base de données locale.

- `migrations/V1__initial_schema_postgresql.sql` : schéma PostgreSQL versionné du projet
- `seeds/demo_data_postgresql.sql` : jeu de données de démonstration et de test manuel
- la base principale de l'application reste PostgreSQL via `docker-compose.yml`

## Livrables SQL

Le livrable SQL demandé pour le projet est couvert ici, sans dépendre de l'arborescence du backend :

- structure : `migrations/V1__initial_schema_postgresql.sql`
- données de test : `seeds/demo_data_postgresql.sql`

Le backend fonctionne encore aujourd'hui avec `spring.jpa.hibernate.ddl-auto=update`, mais ces fichiers
fournissent une version explicite de la structure et d'un jeu de données reproductible pour la démonstration.

## Objectif du schéma versionné

Le fichier `migrations/V1__initial_schema_postgresql.sql` documente :

- les tables métier
- les clés primaires et étrangères
- les contraintes énumérées
- les index utiles aux parcours courants

Il peut servir de base pour une future adoption de Flyway ou Liquibase.

## Objectif du jeu de données

Le fichier `seeds/demo_data_postgresql.sql` reprend les comptes de démonstration et les données métier
utiles pour illustrer :

- les rôles de projet
- les invitations
- les tâches
- l'historique
- les notifications

Le script est idempotent : il peut être rejoué sans dupliquer les données principales.

## Exécution manuelle

Depuis la racine du projet, avec PostgreSQL déjà démarré :

```powershell
docker compose up -d postgres
Get-Content .\database\migrations\V1__initial_schema_postgresql.sql | docker exec -i pmt-postgres psql -U admin -d pmtdb
Get-Content .\database\seeds\demo_data_postgresql.sql | docker exec -i pmt-postgres psql -U admin -d pmtdb
```

Le résultat obtenu est le suivant :

- la structure complète de la base
- les comptes de démonstration
- des projets, tâches, invitations, notifications et historiques prêts pour la soutenance

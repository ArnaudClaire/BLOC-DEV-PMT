# Postman

Ce dossier contient la collection Postman versionnee pour tester l'API PMT.

## Fichiers

- `PMT-Backend.postman_collection.json` : collection principale alignee sur les controllers du projet
- `local.postman_environment.json` : environnement local sans secret

## Variables utiles

La collection utilise notamment :

- `baseUrl`
- `userId`
- `ownerId`
- `assigneeId`
- `projectId`
- `taskId`
- `memberId`
- `invitationId`
- `notificationId`
- `taskHistoryId`

L'environnement local versionne propose aussi ces variables pour pointer rapidement vers les donnees prechargees du projet.

## Authentification

La securite backend est desactivee temporairement pour faciliter le developpement.

Important :
la collection est actuellement configuree sans authentification pour coller au backend de dev. Avant une mise en production, il faudra remettre en place une authentification applicative et realigner la collection.

En pratique :

- toutes les routes peuvent etre testees directement
- les identifiants `basicUsername` et `basicPassword` ne sont plus necessaires tant que ce mode reste actif

## Ordre conseille pour les tests

1. Lancer l'application
2. Appeler `GET /users`, `GET /projects` et `GET /tasks` pour retrouver les IDs seed
3. Mettre a jour les variables Postman si necessaire
4. Utiliser `POST /users` puis les autres endpoints de creation si besoin

## URLs de base

- `http://localhost:8081` pour un lancement Spring Boot local
- `http://localhost:8081` si le backend tourne via `docker compose`

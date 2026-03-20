# Postman

Ce dossier contient la collection Postman versionnee pour tester l'API PMT.

## Fichiers

- `PMT-Backend.postman_collection.json` : collection principale alignee sur les controllers du projet
- `local.postman_environment.json` : environnement local sans secret

## Variables utiles

La collection utilise notamment :

- `baseUrl`
- `loginEmail`
- `loginPassword`
- `userId`
- `ownerId`
- `assigneeId`
- `projectId`
- `taskId`
- `memberId`
- `requestedById`
- `invitationId`
- `invitationToken`
- `notificationId`
- `taskHistoryId`
- `columnId`

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
3. Appeler `POST /auth/login` si tu veux verifier le flux de connexion frontend/backend
4. Mettre a jour les variables Postman si necessaire
5. Utiliser `POST /users` puis les autres endpoints de creation si besoin

## Couverture actuelle de la collection

La collection couvre maintenant aussi :

- `POST /auth/login`
- `PUT /project-members/{id}`
- `GET /projects/{projectId}/project-invitations`
- `GET /project-invitations/token/{token}`
- `POST /project-invitations/token/{token}/accept`
- `POST /project-invitations/{id}/cancel`
- `POST /project-invitations/{id}/resend`
- `PUT /tasks/{id}`
- `GET /task-board-columns`
- `GET /projects/{projectId}/task-board-columns`
- `GET /task-board-columns/{id}`
- `POST /task-board-columns`

Note utile :

- `POST /project-invitations` attend desormais `invitedById`
- les routes par token utilisent la variable `invitationToken`

## URLs de base

- `http://localhost:8081` pour un lancement Spring Boot local
- `http://localhost:8081` si le backend tourne via `docker compose`

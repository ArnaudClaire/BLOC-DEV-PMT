# Postman

Ce dossier contient la collection Postman versionnée pour tester l'API PMT.

## Fichiers

- `PMT-Backend.postman_collection.json` : collection principale alignée sur les contrôleurs du projet
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

L'environnement local versionné propose aussi ces variables pour pointer rapidement vers les données préchargées du projet.

## Authentification

La sécurité backend est désactivée temporairement pour faciliter le développement.

À retenir :

- la collection est actuellement configurée sans authentification pour coller au backend de développement
- avant une mise en production, il faudra remettre une authentification applicative et réaligner la collection
- les identifiants `basicUsername` et `basicPassword` ne sont plus nécessaires tant que ce mode reste actif

## Parcours conseillé

1. Lancer l'application
2. Appeler `GET /users`, `GET /projects` et `GET /tasks` pour retrouver les IDs seed
3. Appeler `POST /auth/login` si tu veux vérifier le flux de connexion frontend/backend
4. Mettre à jour les variables Postman si nécessaire
5. Utiliser `POST /users` puis les autres endpoints de création si besoin

## Couverture actuelle

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

- `POST /project-invitations` attend désormais `invitedById`
- les routes par token utilisent la variable `invitationToken`

## URL de base

- `http://localhost:8081` pour un lancement Spring Boot local
- `http://localhost:8081` si le backend tourne via `docker compose`

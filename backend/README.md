# Backend PMT

API REST Spring Boot pour la gestion des utilisateurs, projets, tâches, invitations et notifications.

## Stack

- Java 17
- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL en local et en développement
- H2 pour les tests

## Port

Le backend démarre sur `http://localhost:8081`.

Configuration source : `backend/src/main/resources/application.yml`

## Variables d'environnement

Le backend attend les variables suivantes :

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/pmtdb
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin
```

Dans le projet, le `docker-compose.yml` expose PostgreSQL sur `localhost:5433`, donc en Docker la valeur ressemble plutôt à :

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/pmtdb
```

## Lancer le backend

Depuis le dossier `backend/` :

```powershell
.\mvnw.cmd spring-boot:run
```

Ou depuis la racine du projet :

```powershell
.\start-dev.ps1
```

## Lancer les tests

```powershell
.\mvnw.cmd test
```

Les tests utilisent H2 en mémoire via `backend/src/test/resources/application.yml`.

## Endpoints principaux

- `POST /auth/login`
- `GET /users`
- `GET /users/{id}`
- `POST /users`
- `DELETE /users/{id}`
- `GET /projects`
- `GET /projects/{id}`
- `POST /projects`
- `DELETE /projects/{id}`
- `GET /tasks`
- `GET /tasks/{id}`
- `POST /tasks`
- `DELETE /tasks/{id}`
- `GET /project-members`
- `POST /project-members`
- `GET /project-invitations`
- `POST /project-invitations`
- `GET /notifications`
- `POST /notifications`
- `GET /task-histories`
- `POST /task-histories`

## Authentification

Le backend expose `POST /auth/login`.

- le mot de passe est vérifié côté backend
- le hashage utilise `BCryptPasswordEncoder`
- la configuration Spring Security actuelle est stateless
- les routes sont actuellement ouvertes en `permitAll()`

Autrement dit, l'endpoint de login valide bien email et mot de passe, mais l'application ne gère pas encore de JWT ou de session sécurisée côté backend.

## Notes utiles

- `ddl-auto` est en `update` à l'exécution
- `spring.sql.init.mode=always` charge les données SQL au démarrage
- la documentation OpenAPI est disponible car `springdoc-openapi-starter-webmvc-ui` est présent dans `pom.xml`

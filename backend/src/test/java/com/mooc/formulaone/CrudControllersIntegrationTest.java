package com.mooc.formulaone;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.models.TaskPriority;
import com.mooc.formulaone.models.TaskStatus;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.ProjectMemberService;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test-user")
/**
 * Couvre les principaux parcours CRUD exposes par les controllers REST
 * en validant les codes HTTP, les charges utiles et la persistance.
 */
class CrudControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskBoardColumnService taskBoardColumnService;

    /**
     * Verifie le cycle complet de vie d'un utilisateur via l'API:
     * creation, listing, lecture detaillee puis suppression.
     */
    @Test
    void shouldCreateListAndDeleteUserThroughApi() throws Exception {
        String userJson = """
                {
                  "username": "alice",
                  "email": "alice@example.com",
                  "password": "secret123"
                }
                """;

        String responseBody = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = Long.valueOf(responseBody);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'alice@example.com')]").isNotEmpty());

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAuthenticateUserThroughApi() throws Exception {
        String userJson = """
                {
                  "username": "auth-user",
                  "email": "auth@example.com",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                  "email": "auth@example.com",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("auth-user"))
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.ownedProjects").doesNotExist());
    }

    @Test
    void shouldAuthenticateUserWithExistingRelationsThroughApi() throws Exception {
        Long ownerId = createUser("seeded-owner", "seeded.owner@example.com", "demo123");
        Long assigneeId = createUser("seeded-member", "seeded.member@example.com", "demo123");
        Long projectId = createProject(ownerId, "Seeded Project");
        createTask(projectId, ownerId, assigneeId);

        String loginJson = """
                {
                  "email": "seeded.owner@example.com",
                  "password": "demo123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerId.intValue()))
                .andExpect(jsonPath("$.username").value("seeded-owner"))
                .andExpect(jsonPath("$.email").value("seeded.owner@example.com"))
                .andExpect(jsonPath("$.ownedProjects").doesNotExist())
                .andExpect(jsonPath("$.projectMemberships").doesNotExist())
                .andExpect(jsonPath("$.createdTasks").doesNotExist());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        String userJson = """
                {
                  "username": "auth-user",
                  "email": "auth-fail@example.com",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());

        String loginJson = """
                {
                  "email": "auth-fail@example.com",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifie qu'un projet peut etre cree, relu et supprime via l'API
     * tout en conservant le lien avec son proprietaire.
     */
    @Test
    void shouldCreateListAndDeleteProjectThroughApi() throws Exception {
        Long ownerId = createUser("owner", "owner@example.com");

        String projectJson = """
                {
                  "name": "PMT",
                  "description": "Gestion",
                  "startDate": "2026-03-16",
                  "ownerId": %d
                }
                """.formatted(ownerId);

        Long projectId = Long.valueOf(mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'PMT')]").isNotEmpty());

        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner.email").value("owner@example.com"));

        mockMvc.perform(delete("/projects/{id}", projectId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/{id}", projectId))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifie le CRUD d'une invitation de projet et la bonne serialisation
     * des informations du projet rattache.
     */
    @Test
    void shouldCreateListAndDeleteProjectInvitationThroughApi() throws Exception {
        Long ownerId = createUser("lead", "lead@example.com");
        Long projectId = createProject(ownerId, "Roadmap");

        String invitationJson = """
                {
                  "email": "new.member@example.com",
                  "role": "MEMBER",
                  "projectId": %d,
                  "invitedById": %d
                }
                """.formatted(projectId, ownerId);

        Long invitationId = Long.valueOf(mockMvc.perform(post("/project-invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invitationJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/project-invitations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.role == 'MEMBER')]").isNotEmpty());

        mockMvc.perform(get("/project-invitations/{id}", invitationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project.name").value("Roadmap"));

        mockMvc.perform(delete("/project-invitations/{id}", invitationId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/project-invitations/{id}", invitationId))
                .andExpect(status().isNotFound());
    }

    /**
     * Verifie qu'un membre de projet deja persiste peut etre consulte
     * puis supprime via les endpoints exposes.
     */
    @Test
    void shouldCreateListAndDeleteProjectMemberThroughApi() throws Exception {
        Long ownerId = createUser("captain", "captain@example.com");
        Long memberUserId = createUser("member", "member@example.com");
        Long projectId = createProject(ownerId, "Delivery");

        com.mooc.formulaone.models.ProjectMember projectMember = new com.mooc.formulaone.models.ProjectMember();
        projectMember.setRole(ProjectRole.ADMIN);
        projectMember.setProject(projectService.findById(projectId));
        projectMember.setUser(userService.findById(memberUserId));
        Long memberId = projectMemberService.create(projectMember);

        mockMvc.perform(get("/project-members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.role == 'ADMIN')]").isNotEmpty());

        mockMvc.perform(get("/project-members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("member@example.com"));

        mockMvc.perform(delete("/project-members/{id}", memberId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/project-members/{id}", memberId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProjectMemberRoleThroughApi() throws Exception {
        Long ownerId = createUser("owner-role", "owner-role@example.com");
        Long memberUserId = createUser("member-role", "member-role@example.com");
        Long projectId = createProject(ownerId, "Roles");

        com.mooc.formulaone.models.ProjectMember projectMember = new com.mooc.formulaone.models.ProjectMember();
        projectMember.setRole(ProjectRole.MEMBER);
        projectMember.setProject(projectService.findById(projectId));
        projectMember.setUser(userService.findById(memberUserId));
        Long memberId = projectMemberService.create(projectMember);

        String updateJson = """
                {
                  "role": "OBSERVER",
                  "requestedById": %d
                }
                """.formatted(ownerId);

        mockMvc.perform(put("/project-members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/project-members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("OBSERVER"));
    }

    /**
     * Verifie qu'un historique de tache cree via HTTP reste consultable
     * avec ses relations avant sa suppression.
     */
    @Test
    void shouldCreateListAndDeleteTaskHistoryThroughApi() throws Exception {
        Long ownerId = createUser("writer", "writer@example.com");
        Long assigneeId = createUser("reader", "reader@example.com");
        Long projectId = createProject(ownerId, "Docs");
        Long taskId = createTask(projectId, ownerId, assigneeId);

        String taskHistoryJson = """
                {
                  "actionType": "STATUS_CHANGED",
                  "fieldName": "status",
                  "oldValue": "TODO",
                  "newValue": "DONE",
                  "taskId": %d,
                  "changedById": %d
                }
                """.formatted(taskId, ownerId);

        Long historyId = Long.valueOf(mockMvc.perform(post("/task-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskHistoryJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());

        mockMvc.perform(get("/task-histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.actionType == 'STATUS_CHANGED')]").isNotEmpty());

        mockMvc.perform(get("/task-histories/{id}", historyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.id").value(taskId.intValue()));

        mockMvc.perform(delete("/task-histories/{id}", historyId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/task-histories/{id}", historyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTaskThroughApi() throws Exception {
        Long ownerId = createUser("owner-update", "owner-update@example.com", "demo123");
        Long assigneeId = createUser("assignee-update", "assignee-update@example.com", "demo123");
        Long projectId = createProject(ownerId, "Update Board");
        Long taskId = createTask(projectId, ownerId, assigneeId);

        String updateJson = """
                {
                  "title": "Tache mise a jour",
                  "description": "Description de mise a jour suffisamment longue",
                  "status": "DONE",
                  "priority": "LOW",
                  "dueDate": "2026-04-15",
                  "endDate": "2026-04-16",
                  "assignedToId": %d
                }
                """.formatted(ownerId);

        mockMvc.perform(put("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tache mise a jour"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.assignedTo.email").value("owner-update@example.com"));
    }

    /**
     * Cree un utilisateur minimal utilisable dans les scenarios
     * d'integration qui ont besoin de relations persistantes.
     *
     * @param username nom fonctionnel de l'utilisateur
     * @param email adresse email associee
     * @return l'identifiant genere en base
     */
    private Long createUser(String username, String email) {
        return createUser(username, email, "hash");
    }

    /**
     * Cree un utilisateur avec le mot de passe brut fourni afin de couvrir
     * les scenarios d'authentification et les graphes deja rattaches.
     *
     * @param username nom fonctionnel de l'utilisateur
     * @param email adresse email associee
     * @param rawPassword mot de passe brut a encoder
     * @return l'identifiant genere en base
     */
    private Long createUser(String username, String email, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(rawPassword);
        return userService.create(user);
    }

    /**
     * Cree un projet de test rattache a son proprietaire.
     *
     * @param ownerId identifiant du proprietaire
     * @param name nom du projet
     * @return l'identifiant du projet cree
     */
    private Long createProject(Long ownerId, String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(name + " description");
        project.setOwner(userService.findById(ownerId));
        Long projectId = projectService.create(project);
        createDefaultColumns(projectService.findById(projectId));
        return projectId;
    }

    /**
     * Cree une tache complete afin d'alimenter les tests qui dependent
     * d'un projet et de deux utilisateurs deja existants.
     *
     * @param projectId identifiant du projet cible
     * @param createdById identifiant du createur
     * @param assignedToId identifiant de l'utilisateur assigne
     * @return l'identifiant de la tache creee
     */
    private Long createTask(Long projectId, Long createdById, Long assignedToId) {
        Task task = new Task();
        task.setTitle("Write docs");
        task.setDescription("Document API");
        task.setStatus("TODO");
        task.setPriority(TaskPriority.MEDIUM);
        task.setDueDate(LocalDate.of(2026, 4, 1));
        task.setProject(projectService.findById(projectId));
        task.setCreatedBy(userService.findById(createdById));
        task.setAssignedTo(userService.findById(assignedToId));
        return taskService.create(task);
    }

    private void createDefaultColumns(Project project) {
        String[] defaultColumns = { "TODO", "IN_PROGRESS", "DONE" };
        for (int index = 0; index < defaultColumns.length; index++) {
            com.mooc.formulaone.models.TaskBoardColumn column = new com.mooc.formulaone.models.TaskBoardColumn();
            column.setName(defaultColumns[index]);
            column.setDisplayOrder(index);
            column.setProject(project);
            taskBoardColumnService.create(column);
        }
    }
}

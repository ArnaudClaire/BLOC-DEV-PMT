package com.mooc.formulaone;

import com.mooc.formulaone.controllers.NotificationController;
import com.mooc.formulaone.controllers.ProjectController;
import com.mooc.formulaone.controllers.ProjectInvitationController;
import com.mooc.formulaone.controllers.ProjectMemberController;
import com.mooc.formulaone.controllers.TaskController;
import com.mooc.formulaone.controllers.TaskHistoryController;
import com.mooc.formulaone.controllers.AuthController;
import com.mooc.formulaone.bootstrap.TaskBoardColumnBootstrap;
import com.mooc.formulaone.controllers.dto.AuthLoginRequest;
import com.mooc.formulaone.controllers.dto.AuthLoginResponse;
import com.mooc.formulaone.controllers.dto.NotificationCreateRequest;
import com.mooc.formulaone.controllers.dto.ProjectCreateRequest;
import com.mooc.formulaone.controllers.dto.ProjectInvitationCreateRequest;
import com.mooc.formulaone.controllers.dto.ProjectMemberCreateRequest;
import com.mooc.formulaone.controllers.dto.TaskCreateRequest;
import com.mooc.formulaone.controllers.dto.TaskHistoryCreateRequest;
import com.mooc.formulaone.controllers.dto.TaskUpdateRequest;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.models.NotificationStatus;
import com.mooc.formulaone.models.NotificationType;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.models.TaskHistory;
import com.mooc.formulaone.models.TaskHistoryAction;
import com.mooc.formulaone.models.TaskPriority;
import com.mooc.formulaone.models.TaskStatus;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.NotificationService;
import com.mooc.formulaone.services.ProjectInvitationService;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.ProjectMemberService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import com.mooc.formulaone.services.TaskHistoryService;
import com.mooc.formulaone.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/**
 * Documente les tests unitaires des controllers en verifiant qu'ils
 * transforment correctement les requetes entrantes avant de deleguer
 * la persistence et la lecture aux services metiers.
 */
class ControllersUnitTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskBoardColumnService taskBoardColumnService;

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProjectInvitationService projectInvitationService;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private TaskBoardColumnBootstrap taskBoardColumnBootstrap;

    @InjectMocks
    private TaskController taskController;

    @InjectMocks
    private ProjectController projectController;

    @InjectMocks
    private ProjectInvitationController projectInvitationController;

    @InjectMocks
    private ProjectMemberController projectMemberController;

    @InjectMocks
    private TaskHistoryController taskHistoryController;

    @InjectMocks
    private NotificationController notificationController;

    @InjectMocks
    private AuthController authController;

    /**
     * Verifie que la creation d'une tache mappe correctement le DTO HTTP
     * vers l'entite metier avant de deleguer au service.
     */
    @Test
    void shouldDelegateTaskCreateToService() {
        Project project = new Project();
        User creator = new User();
        User assignee = new User();
        TaskCreateRequest request = new TaskCreateRequest(
                "Titre",
                "Description",
                "IN_PROGRESS",
                TaskPriority.HIGH,
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21),
                11L,
                12L,
                13L
        );
        when(projectService.findById(11L)).thenReturn(project);
        when(userService.findById(12L)).thenReturn(creator);
        when(userService.findById(13L)).thenReturn(assignee);
        TaskBoardColumn column = new TaskBoardColumn();
        column.setName("IN_PROGRESS");
        when(taskBoardColumnService.findByProjectId(11L)).thenReturn(List.of(column));
        when(taskService.create(any(Task.class))).thenReturn(42L);

        Long id = taskController.create(request);

        assertThat(id).isEqualTo(42L);
        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).create(taskCaptor.capture());

        Task createdTask = taskCaptor.getValue();
        assertThat(createdTask.getTitle()).isEqualTo("Titre");
        assertThat(createdTask.getDescription()).isEqualTo("Description");
        assertThat(createdTask.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(createdTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(createdTask.getDueDate()).isEqualTo(LocalDate.of(2026, 3, 20));
        assertThat(createdTask.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 21));
        assertThat(createdTask.getProject()).isSameAs(project);
        assertThat(createdTask.getCreatedBy()).isSameAs(creator);
        assertThat(createdTask.getAssignedTo()).isSameAs(assignee);
    }

    /**
     * Verifie que les lectures et la suppression de tache se limitent
     * a deleguer au service sans logique supplementaire inattendue.
     */
    @Test
    void shouldDelegateTaskReadOperationsToService() {
        Task task = new Task();
        when(taskService.findAll()).thenReturn(List.of(task));
        when(taskService.findById(5L)).thenReturn(task);

        assertThat(taskController.findAll()).containsExactly(task);
        assertThat(taskController.findById(5L)).isSameAs(task);
        taskController.delete(5L);

        verify(taskService).findAll();
        verify(taskService, times(2)).findById(5L);
        verify(taskService).delete(task);
    }

    @Test
    void shouldDelegateTaskUpdateToService() {
        Task task = new Task();
        Project project = new Project();
        project.setId(5L);
        task.setProject(project);
        User assignee = new User();
        TaskBoardColumn column = new TaskBoardColumn();
        column.setName("DONE");
        TaskUpdateRequest request = new TaskUpdateRequest(
                "Titre mis a jour",
                "Description mise a jour",
                "DONE",
                TaskPriority.LOW,
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 3),
                13L
        );
        when(taskService.findById(5L)).thenReturn(task);
        when(userService.findById(13L)).thenReturn(assignee);
        when(taskBoardColumnService.findByProjectId(5L)).thenReturn(List.of(column));

        taskController.update(5L, request);

        assertThat(task.getTitle()).isEqualTo("Titre mis a jour");
        assertThat(task.getDescription()).isEqualTo("Description mise a jour");
        assertThat(task.getStatus()).isEqualTo("DONE");
        assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 4, 2));
        assertThat(task.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 3));
        assertThat(task.getAssignedTo()).isSameAs(assignee);
        verify(taskService).update(task);
    }

    /**
     * Verifie que le controller de membres de projet construit bien
     * l'entite a partir des identifiants recus dans la requete.
     */
    @Test
    void shouldDelegateProjectMemberCreateToService() {
        Instant joinedAt = Instant.parse("2026-03-16T17:30:00Z");
        Project project = new Project();
        User user = new User();
        ProjectMemberCreateRequest request = new ProjectMemberCreateRequest(
                ProjectRole.ADMIN,
                joinedAt,
                21L,
                22L
        );
        when(projectService.findById(21L)).thenReturn(project);
        when(userService.findById(22L)).thenReturn(user);
        when(projectMemberService.create(any(ProjectMember.class))).thenReturn(7L);

        Long id = projectMemberController.create(request);

        assertThat(id).isEqualTo(7L);
        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberService).create(memberCaptor.capture());

        ProjectMember createdMember = memberCaptor.getValue();
        assertThat(createdMember.getRole()).isEqualTo(ProjectRole.ADMIN);
        assertThat(createdMember.getJoinedAt()).isEqualTo(Timestamp.from(joinedAt));
        assertThat(createdMember.getProject()).isSameAs(project);
        assertThat(createdMember.getUser()).isSameAs(user);
    }

    /**
     * Verifie que le controller de projet reconstruit correctement
     * l'entite depuis le DTO d'entree.
     */
    @Test
    void shouldDelegateProjectCreateToService() {
        User owner = new User();
        ProjectCreateRequest request = new ProjectCreateRequest(
                "PMT",
                "Projet de demonstration",
                LocalDate.of(2026, 3, 16),
                31L
        );
        when(userService.findById(31L)).thenReturn(owner);
        Project persistedProject = new Project();
        persistedProject.setOwner(owner);
        when(projectService.create(any(Project.class))).thenReturn(12L);
        when(projectService.findById(12L)).thenReturn(persistedProject);
        when(projectMemberService.create(any(ProjectMember.class))).thenReturn(44L);

        Long id = projectController.create(request);

        assertThat(id).isEqualTo(12L);
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectService).create(projectCaptor.capture());

        Project createdProject = projectCaptor.getValue();
        assertThat(createdProject.getName()).isEqualTo("PMT");
        assertThat(createdProject.getDescription()).isEqualTo("Projet de demonstration");
        assertThat(createdProject.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(createdProject.getOwner()).isSameAs(owner);

        ArgumentCaptor<ProjectMember> projectMemberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberService).create(projectMemberCaptor.capture());

        ProjectMember createdProjectMember = projectMemberCaptor.getValue();
        assertThat(createdProjectMember.getRole()).isEqualTo(ProjectRole.ADMIN);
        assertThat(createdProjectMember.getProject()).isSameAs(persistedProject);
        assertThat(createdProjectMember.getUser()).isSameAs(owner);
    }

    /**
     * Verifie le mapping du controller d'invitations de projet.
     */
    @Test
    void shouldDelegateProjectInvitationCreateToService() {
        Project project = new Project();
        User invitedBy = new User();
        ProjectInvitationCreateRequest request = new ProjectInvitationCreateRequest(
                "invitee@example.com",
                ProjectRole.MEMBER,
                41L,
                42L
        );
        when(projectService.findById(41L)).thenReturn(project);
        when(userService.findById(42L)).thenReturn(invitedBy);
        when(projectInvitationService.create(any(ProjectInvitation.class))).thenReturn(15L);

        Long id = projectInvitationController.create(request);

        assertThat(id).isEqualTo(15L);
        ArgumentCaptor<ProjectInvitation> invitationCaptor = ArgumentCaptor.forClass(ProjectInvitation.class);
        verify(projectInvitationService).create(invitationCaptor.capture());

        ProjectInvitation createdInvitation = invitationCaptor.getValue();
        assertThat(createdInvitation.getEmail()).isEqualTo("invitee@example.com");
        assertThat(createdInvitation.getRole()).isEqualTo(ProjectRole.MEMBER);
        assertThat(createdInvitation.getProject()).isSameAs(project);
        assertThat(createdInvitation.getInvitedBy()).isSameAs(invitedBy);
    }

    /**
     * Verifie le mapping du controller d'historique de tache.
     */
    @Test
    void shouldDelegateTaskHistoryCreateToService() {
        Task task = new Task();
        User changedBy = new User();
        TaskHistoryCreateRequest request = new TaskHistoryCreateRequest(
                TaskHistoryAction.STATUS_CHANGED,
                "status",
                "TODO",
                "DONE",
                51L,
                52L
        );
        when(taskService.findById(51L)).thenReturn(task);
        when(userService.findById(52L)).thenReturn(changedBy);
        when(taskHistoryService.create(any(TaskHistory.class))).thenReturn(18L);

        Long id = taskHistoryController.create(request);

        assertThat(id).isEqualTo(18L);
        ArgumentCaptor<TaskHistory> historyCaptor = ArgumentCaptor.forClass(TaskHistory.class);
        verify(taskHistoryService).create(historyCaptor.capture());

        TaskHistory createdHistory = historyCaptor.getValue();
        assertThat(createdHistory.getActionType()).isEqualTo(TaskHistoryAction.STATUS_CHANGED);
        assertThat(createdHistory.getFieldName()).isEqualTo("status");
        assertThat(createdHistory.getOldValue()).isEqualTo("TODO");
        assertThat(createdHistory.getNewValue()).isEqualTo("DONE");
        assertThat(createdHistory.getTask()).isSameAs(task);
        assertThat(createdHistory.getChangedBy()).isSameAs(changedBy);
    }

    /**
     * Verifie que le controller de notifications relaie correctement
     * le mapping de creation et les operations CRUD de base au service.
     */
    @Test
    void shouldDelegateNotificationCrudOperationsToService() {
        Notification notification = new Notification();
        User user = new User();
        Task task = new Task();
        NotificationCreateRequest request = new NotificationCreateRequest(
                NotificationType.TASK_ASSIGNED,
                NotificationStatus.SENT,
                "Assigned",
                Instant.parse("2026-03-16T11:00:00Z"),
                61L,
                62L
        );
        when(notificationService.findAll()).thenReturn(List.of(notification));
        when(notificationService.findById(9L)).thenReturn(notification);
        when(userService.findById(61L)).thenReturn(user);
        when(taskService.findById(62L)).thenReturn(task);
        when(notificationService.create(any(Notification.class))).thenReturn(9L);

        assertThat(notificationController.findAll()).containsExactly(notification);
        assertThat(notificationController.findById(9L)).isSameAs(notification);
        assertThat(notificationController.create(request)).isEqualTo(9L);
        notificationController.delete(9L);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).create(notificationCaptor.capture());
        Notification createdNotification = notificationCaptor.getValue();
        assertThat(createdNotification.getType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(createdNotification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(createdNotification.getMessage()).isEqualTo("Assigned");
        assertThat(createdNotification.getSentAt()).isEqualTo(Timestamp.from(Instant.parse("2026-03-16T11:00:00Z")));
        assertThat(createdNotification.getUser()).isSameAs(user);
        assertThat(createdNotification.getTask()).isSameAs(task);
        verify(notificationService).findAll();
        verify(notificationService, times(2)).findById(9L);
        verify(notificationService).delete(notification);
    }

    @Test
    void shouldDelegateAuthenticationToUserService() {
        User user = new User();
        user.setId(8L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        AuthLoginRequest request = new AuthLoginRequest("alice@example.com", "secret123");
        when(userService.authenticate("alice@example.com", "secret123")).thenReturn(user);

        AuthLoginResponse authenticatedUser = authController.login(request);

        assertThat(authenticatedUser.id()).isEqualTo(8L);
        assertThat(authenticatedUser.username()).isEqualTo("alice");
        assertThat(authenticatedUser.email()).isEqualTo("alice@example.com");
        verify(userService).authenticate("alice@example.com", "secret123");
    }
}

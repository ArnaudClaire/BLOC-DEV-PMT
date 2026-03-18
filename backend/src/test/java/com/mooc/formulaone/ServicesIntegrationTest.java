package com.mooc.formulaone;

import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.InvitationStatus;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.models.NotificationStatus;
import com.mooc.formulaone.models.NotificationType;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.models.TaskHistory;
import com.mooc.formulaone.models.TaskHistoryAction;
import com.mooc.formulaone.models.TaskPriority;
import com.mooc.formulaone.models.TaskStatus;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.NotificationService;
import com.mooc.formulaone.services.ProjectInvitationService;
import com.mooc.formulaone.services.ProjectMemberService;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskHistoryService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
/**
 * Valide les services sur un contexte Spring complet en couvrant
 * les parcours CRUD et les branches d'erreur "not found".
 */
class ServicesIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectInvitationService projectInvitationService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Verifie que chaque service persiste, relit puis supprime correctement
     * ses entites et qu'une lecture apres suppression leve bien l'exception attendue.
     */
    @Test
    void shouldCoverAllCrudServicesIncludingNotFoundBranches() {
        User owner = new User();
        owner.setUsername("owner");
        owner.setEmail("owner@test.local");
        owner.setPasswordHash("hash");
        Long ownerId = userService.create(owner);

        User assignee = new User();
        assignee.setUsername("assignee");
        assignee.setEmail("assignee@test.local");
        assignee.setPasswordHash("hash");
        Long assigneeId = userService.create(assignee);

        assertThat(userService.findAll())
                .extracting(User::getEmail)
                .contains("owner@test.local", "assignee@test.local");
        assertThat(userService.findById(ownerId).getUsername()).isEqualTo("owner");

        Project project = new Project();
        project.setName("Coverage");
        project.setDescription("Full coverage");
        project.setStartDate(LocalDate.of(2026, 3, 16));
        project.setOwner(userService.findById(ownerId));
        Long projectId = projectService.create(project);

        assertThat(projectService.findAll())
                .extracting(Project::getName)
                .contains("Coverage");
        assertThat(projectService.findById(projectId).getName()).isEqualTo("Coverage");

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setEmail("invitee@test.local");
        invitation.setRole(ProjectRole.OBSERVER);
        invitation.setProject(projectService.findById(projectId));
        invitation.setInvitedBy(userService.findById(ownerId));
        Long invitationId = projectInvitationService.create(invitation);

        assertThat(projectInvitationService.findAll())
                .extracting(ProjectInvitation::getEmail)
                .contains("invitee@test.local");
        assertThat(projectInvitationService.findById(invitationId).getEmail()).isEqualTo("invitee@test.local");

        ProjectMember projectMember = new ProjectMember();
        projectMember.setRole(ProjectRole.MEMBER);
        projectMember.setJoinedAt(Timestamp.from(Instant.parse("2026-03-16T10:15:30Z")));
        projectMember.setProject(projectService.findById(projectId));
        projectMember.setUser(userService.findById(assigneeId));
        Long memberId = projectMemberService.create(projectMember);

        assertThat(projectMemberService.findAll())
                .extracting(member -> member.getUser().getEmail())
                .contains("assignee@test.local");
        assertThat(projectMemberService.findById(memberId).getUser().getEmail()).isEqualTo("assignee@test.local");

        Task task = new Task();
        task.setTitle("Cover code");
        task.setDescription("Exercise services");
        task.setStatus("IN_PROGRESS");
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(LocalDate.of(2026, 4, 10));
        task.setEndDate(LocalDate.of(2026, 4, 15));
        task.setProject(projectService.findById(projectId));
        task.setCreatedBy(userService.findById(ownerId));
        task.setAssignedTo(userService.findById(assigneeId));
        Long taskId = taskService.create(task);

        assertThat(taskService.findAll())
                .extracting(Task::getTitle)
                .contains("Cover code");
        assertThat(taskService.findById(taskId).getStatus()).isEqualTo("IN_PROGRESS");

        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setActionType(TaskHistoryAction.ASSIGNED);
        taskHistory.setFieldName("assignedTo");
        taskHistory.setOldValue("nobody");
        taskHistory.setNewValue("assignee");
        taskHistory.setTask(taskService.findById(taskId));
        taskHistory.setChangedBy(userService.findById(ownerId));
        Long historyId = taskHistoryService.create(taskHistory);

        assertThat(taskHistoryService.findAll())
                .extracting(TaskHistory::getActionType)
                .contains(TaskHistoryAction.ASSIGNED);
        assertThat(taskHistoryService.findById(historyId).getActionType()).isEqualTo(TaskHistoryAction.ASSIGNED);

        Notification notification = new Notification();
        notification.setType(NotificationType.INVITATION_SENT);
        notification.setStatus(NotificationStatus.READ);
        notification.setMessage("Invitation sent");
        notification.setSentAt(Timestamp.from(Instant.parse("2026-03-16T11:00:00Z")));
        notification.setUser(userService.findById(ownerId));
        notification.setTask(taskService.findById(taskId));
        Long notificationId = notificationService.create(notification);

        assertThat(notificationService.findAll())
                .extracting(Notification::getMessage)
                .contains("Invitation sent");
        assertThat(notificationService.findById(notificationId).getMessage()).isEqualTo("Invitation sent");

        notificationService.delete(notificationService.findById(notificationId));
        taskHistoryService.delete(taskHistoryService.findById(historyId));
        taskService.delete(taskService.findById(taskId));
        projectMemberService.delete(projectMemberService.findById(memberId));
        projectInvitationService.delete(projectInvitationService.findById(invitationId));
        projectService.delete(projectService.findById(projectId));
        userService.delete(userService.findById(assigneeId));
        userService.delete(userService.findById(ownerId));

        assertThatThrownBy(() -> notificationService.findById(notificationId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> taskHistoryService.findById(historyId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> taskService.findById(taskId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> projectMemberService.findById(memberId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> projectInvitationService.findById(invitationId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> projectService.findById(projectId)).isInstanceOf(EntityDontExistException.class);
        assertThatThrownBy(() -> userService.findById(ownerId)).isInstanceOf(EntityDontExistException.class);
    }
}

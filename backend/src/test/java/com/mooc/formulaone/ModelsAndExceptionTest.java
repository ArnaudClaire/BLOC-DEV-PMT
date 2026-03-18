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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModelsAndExceptionTest {

    /**
     * Verifie les accesseurs des entites, les relations bidirectionnelles,
     * les callbacks d'audit et l'exhaustivite des enums du domaine.
     */
    @Test
    void shouldCoverEntitiesEnumsAndAuditHooks() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("hash");

        Project project = new Project();
        project.setId(2L);
        project.setName("PMT");
        project.setDescription("Project management");
        project.setStartDate(LocalDate.of(2026, 3, 16));
        project.setOwner(user);

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setId(3L);
        invitation.setEmail("guest@example.com");
        invitation.setRole(ProjectRole.ADMIN);
        invitation.sendInvitation("token-123", Timestamp.from(Instant.parse("2026-03-23T10:00:00Z")));
        invitation.setProject(project);

        ProjectMember projectMember = new ProjectMember();
        projectMember.setId(4L);
        projectMember.assignRole(ProjectRole.MEMBER);
        projectMember.setJoinedAt(Timestamp.valueOf("2026-03-16 10:00:00"));
        projectMember.setProject(project);
        projectMember.setUser(user);

        Task task = new Task();
        task.setId(5L);
        task.updateTask(
                "Task",
                "Description",
                "DONE",
                TaskPriority.LOW,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 2)
        );
        task.setProject(project);
        task.setCreatedBy(user);
        task.assignTo(user);

        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setId(6L);
        taskHistory.recordChange(
                TaskHistoryAction.COMPLETED,
                "status",
                "IN_PROGRESS",
                "DONE",
                task,
                user
        );

        Notification notification = new Notification();
        notification.setId(7L);
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setMessage("Assigned");
        notification.setUser(user);
        notification.setTask(task);
        notification.send();

        project.setInvitations(Set.of(invitation));
        project.setMembers(Set.of(projectMember));
        project.setTasks(Set.of(task));
        task.setHistories(Set.of(taskHistory));
        task.setNotifications(Set.of(notification));
        user.setOwnedProjects(Set.of(project));
        user.setProjectMemberships(Set.of(projectMember));
        user.setCreatedTasks(Set.of(task));
        user.setAssignedTasks(Set.of(task));
        user.setTaskHistories(Set.of(taskHistory));
        user.setNotifications(Set.of(notification));

        user.onCreate();
        Timestamp createdAt = user.getCreatedAt();
        Timestamp updatedAt = user.getUpdatedAt();
        user.onUpdate();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getOwnedProjects()).containsExactly(project);
        assertThat(user.getProjectMemberships()).containsExactly(projectMember);
        assertThat(user.getCreatedTasks()).containsExactly(task);
        assertThat(user.getAssignedTasks()).containsExactly(task);
        assertThat(user.getTaskHistories()).containsExactly(taskHistory);
        assertThat(user.getNotifications()).containsExactly(notification);
        assertThat(project.getId()).isEqualTo(2L);
        assertThat(project.getName()).isEqualTo("PMT");
        assertThat(project.getDescription()).isEqualTo("Project management");
        assertThat(project.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(project.getOwner()).isEqualTo(user);
        assertThat(project.getInvitations()).containsExactly(invitation);
        assertThat(project.getMembers()).containsExactly(projectMember);
        assertThat(project.getTasks()).containsExactly(task);
        assertThat(invitation.getId()).isEqualTo(3L);
        assertThat(invitation.getEmail()).isEqualTo("guest@example.com");
        assertThat(invitation.getRole()).isEqualTo(ProjectRole.ADMIN);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.getProject()).isEqualTo(project);
        assertThat(projectMember.getId()).isEqualTo(4L);
        assertThat(projectMember.getRole()).isEqualTo(ProjectRole.MEMBER);
        assertThat(projectMember.getJoinedAt()).isEqualTo(Timestamp.valueOf("2026-03-16 10:00:00"));
        assertThat(projectMember.getProject()).isEqualTo(project);
        assertThat(projectMember.getUser()).isEqualTo(user);
        assertThat(task.getId()).isEqualTo(5L);
        assertThat(task.getTitle()).isEqualTo("Task");
        assertThat(task.getDescription()).isEqualTo("Description");
        assertThat(task.getStatus()).isEqualTo("DONE");
        assertThat(task.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(task.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 2));
        assertThat(task.getProject()).isEqualTo(project);
        assertThat(task.getCreatedBy()).isEqualTo(user);
        assertThat(task.getAssignedTo()).isEqualTo(user);
        assertThat(task.getHistories()).containsExactly(taskHistory);
        assertThat(task.getNotifications()).containsExactly(notification);
        assertThat(taskHistory.getId()).isEqualTo(6L);
        assertThat(taskHistory.getActionType()).isEqualTo(TaskHistoryAction.COMPLETED);
        assertThat(taskHistory.getFieldName()).isEqualTo("status");
        assertThat(taskHistory.getOldValue()).isEqualTo("IN_PROGRESS");
        assertThat(taskHistory.getNewValue()).isEqualTo("DONE");
        assertThat(taskHistory.getTask()).isEqualTo(task);
        assertThat(taskHistory.getChangedBy()).isEqualTo(user);
        assertThat(notification.getId()).isEqualTo(7L);
        assertThat(notification.getType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getMessage()).isEqualTo("Assigned");
        assertThat(notification.getSentAt()).isNotNull();
        assertThat(notification.getUser()).isEqualTo(user);
        assertThat(notification.getTask()).isEqualTo(task);
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(updatedAt);
        assertThat(InvitationStatus.values()).containsExactly(
                InvitationStatus.PENDING,
                InvitationStatus.ACCEPTED,
                InvitationStatus.DECLINED,
                InvitationStatus.EXPIRED,
                InvitationStatus.CANCELED
        );
        assertThat(NotificationStatus.values()).containsExactly(NotificationStatus.SENT, NotificationStatus.READ);
        assertThat(NotificationType.values()).containsExactly(NotificationType.TASK_ASSIGNED, NotificationType.INVITATION_SENT);
        assertThat(ProjectRole.values()).containsExactly(ProjectRole.ADMIN, ProjectRole.MEMBER, ProjectRole.OBSERVER);
        assertThat(TaskHistoryAction.values()).containsExactly(TaskHistoryAction.CREATED, TaskHistoryAction.UPDATED, TaskHistoryAction.ASSIGNED, TaskHistoryAction.STATUS_CHANGED, TaskHistoryAction.COMPLETED);
        assertThat(TaskPriority.values()).containsExactly(TaskPriority.LOW, TaskPriority.MEDIUM, TaskPriority.HIGH);
        assertThat(TaskStatus.values()).containsExactly(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.DONE);
    }

    /**
     * Verifie les transitions metier simples exposees par l'invitation.
     */
    @Test
    void shouldHandleInvitationTransitions() {
        ProjectInvitation invitation = new ProjectInvitation();
        User user = new User();
        user.setEmail("guest@example.com");

        invitation.sendInvitation("token-456", Timestamp.from(Instant.parse("2026-03-24T10:00:00Z")));
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);

        invitation.accept(user, Timestamp.from(Instant.parse("2026-03-18T10:00:00Z")));
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);

        invitation.decline();
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);
    }

    /**
     * Verifie que l'exception metier est bien exposee comme une erreur HTTP 404
     * afin que les controllers renvoient un statut coherent en cas d'absence.
     */
    @Test
    void shouldDeclareNotFoundResponseStatusOnException() {
        ResponseStatus responseStatus = EntityDontExistException.class.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

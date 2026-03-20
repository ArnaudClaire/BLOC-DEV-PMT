package com.mooc.formulaone;

import com.mooc.formulaone.dao.ProjectInvitationRepository;
import com.mooc.formulaone.dao.ProjectMemberRepository;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.InvitationStatus;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.ProjectInvitationEmailService;
import com.mooc.formulaone.services.impl.ProjectInvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationServiceImplTest {

    @Mock
    private ProjectInvitationRepository projectInvitationRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectInvitationEmailService projectInvitationEmailService;

    private ProjectInvitationServiceImpl service;

    private Project project;
    private User owner;
    private User invitedUser;

    @BeforeEach
    void setUp() {
        service = new ProjectInvitationServiceImpl(
                projectInvitationRepository,
                projectMemberRepository,
                projectInvitationEmailService
        );

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setUsername("owner");

        invitedUser = new User();
        invitedUser.setId(2L);
        invitedUser.setEmail("invitee@example.com");
        invitedUser.setUsername("invitee");

        project = new Project();
        project.setId(10L);
        project.setName("PMT Launch");
        project.setOwner(owner);
    }

    @Test
    void shouldCreateInvitationAndSendEmail() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setEmail(" Invitee@Example.com ");
        invitation.setRole(ProjectRole.MEMBER);
        invitation.setProject(project);
        invitation.setInvitedBy(owner);

        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 1L, ProjectRole.ADMIN)).thenReturn(Optional.empty());
        when(projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(10L, "invitee@example.com")).thenReturn(false);
        when(projectInvitationRepository.existsByProjectIdAndEmailIgnoreCaseAndStatus(10L, "invitee@example.com", InvitationStatus.PENDING)).thenReturn(false);
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> {
            ProjectInvitation saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Long createdId = service.create(invitation);

        ArgumentCaptor<ProjectInvitation> captor = ArgumentCaptor.forClass(ProjectInvitation.class);
        verify(projectInvitationRepository).save(captor.capture());
        verify(projectInvitationEmailService).sendInvitation(captor.getValue());
        assertThat(createdId).isEqualTo(99L);
        assertThat(captor.getValue().getEmail()).isEqualTo("invitee@example.com");
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(captor.getValue().getToken()).isNotBlank();
    }

    @Test
    void shouldRejectDuplicatePendingInvitationOnCreate() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setEmail("invitee@example.com");
        invitation.setRole(ProjectRole.MEMBER);
        invitation.setProject(project);
        invitation.setInvitedBy(owner);

        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 1L, ProjectRole.ADMIN)).thenReturn(Optional.empty());
        when(projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(10L, "invitee@example.com")).thenReturn(false);
        when(projectInvitationRepository.existsByProjectIdAndEmailIgnoreCaseAndStatus(10L, "invitee@example.com", InvitationStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.create(invitation))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invitation active");
    }

    @Test
    void shouldAcceptPendingInvitationAndCreateProjectMember() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setId(55L);
        invitation.setEmail("invitee@example.com");
        invitation.setRole(ProjectRole.OBSERVER);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setProject(project);
        invitation.setInvitedBy(owner);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plus(Duration.ofDays(1))));

        when(projectInvitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));
        when(projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(10L, "invitee@example.com")).thenReturn(false);
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectInvitation accepted = service.accept("token-1", invitedUser);

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(ProjectRole.OBSERVER);
        assertThat(memberCaptor.getValue().getUser()).isEqualTo(invitedUser);
        assertThat(accepted.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(accepted.getAcceptedBy()).isEqualTo(invitedUser);
    }

    @Test
    void shouldRejectInvitationAcceptanceForAnotherEmail() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setEmail("invitee@example.com");
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setProject(project);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plus(Duration.ofDays(1))));

        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("other@example.com");

        when(projectInvitationRepository.findByToken("token-2")).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> service.accept("token-2", otherUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("autre adresse email");
    }

    @Test
    void shouldRejectAcceptedInvitationResend() {
        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setId(56L);
        invitation.setEmail("invitee@example.com");
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setProject(project);

        when(projectInvitationRepository.findById(56L)).thenReturn(Optional.of(invitation));
        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 1L, ProjectRole.ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resend(56L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deja acceptee");
        verify(projectInvitationEmailService, never()).sendInvitation(any(ProjectInvitation.class));
    }

    @Test
    void shouldExpireOverduePendingInvitationsOnRead() {
        ProjectInvitation expiredInvitation = new ProjectInvitation();
        expiredInvitation.setId(57L);
        expiredInvitation.setEmail("invitee@example.com");
        expiredInvitation.setStatus(InvitationStatus.PENDING);
        expiredInvitation.setProject(project);
        expiredInvitation.setExpiresAt(Timestamp.from(Instant.now().minus(Duration.ofDays(1))));

        when(projectInvitationRepository.findByProjectIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(expiredInvitation));
        when(projectInvitationRepository.save(any(ProjectInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<ProjectInvitation> invitations = service.findByProjectId(10L);

        verify(projectInvitationRepository).save(expiredInvitation);
        assertThat(invitations).singleElement().extracting(ProjectInvitation::getStatus).isEqualTo(InvitationStatus.EXPIRED);
    }
}

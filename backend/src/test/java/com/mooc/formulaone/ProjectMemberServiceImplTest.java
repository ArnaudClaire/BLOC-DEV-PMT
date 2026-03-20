package com.mooc.formulaone;

import com.mooc.formulaone.dao.ProjectMemberRepository;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.impl.ProjectMemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    private ProjectMemberServiceImpl service;

    private Project project;
    private User owner;
    private ProjectMember member;

    @BeforeEach
    void setUp() {
        service = new ProjectMemberServiceImpl(projectMemberRepository);

        owner = new User();
        owner.setId(1L);

        User memberUser = new User();
        memberUser.setId(2L);

        project = new Project();
        project.setId(10L);
        project.setOwner(owner);

        member = new ProjectMember();
        member.setId(20L);
        member.setProject(project);
        member.setUser(memberUser);
        member.setRole(ProjectRole.MEMBER);
    }

    @Test
    void shouldUpdateMemberRoleWhenRequesterIsProjectOwner() {
        when(projectMemberRepository.findById(20L)).thenReturn(Optional.of(member));
        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 1L, ProjectRole.ADMIN)).thenReturn(Optional.empty());
        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectMember updatedMember = service.updateRole(20L, ProjectRole.OBSERVER, 1L);

        assertThat(updatedMember.getRole()).isEqualTo(ProjectRole.OBSERVER);
    }

    @Test
    void shouldRejectRoleChangesWhenRequesterIsNotAdmin() {
        when(projectMemberRepository.findById(20L)).thenReturn(Optional.of(member));
        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 99L, ProjectRole.ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRole(20L, ProjectRole.ADMIN, 99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("administrateur");
    }

    @Test
    void shouldRejectOwnerRoleChanges() {
        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setId(21L);
        ownerMembership.setProject(project);
        ownerMembership.setUser(owner);
        ownerMembership.setRole(ProjectRole.ADMIN);

        when(projectMemberRepository.findById(21L)).thenReturn(Optional.of(ownerMembership));
        when(projectMemberRepository.findByProjectIdAndUserIdAndRole(10L, 1L, ProjectRole.ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRole(21L, ProjectRole.OBSERVER, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("proprietaire");
    }
}

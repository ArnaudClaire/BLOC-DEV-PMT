package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.ProjectMemberRepository;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.services.ProjectMemberService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implémentation CRUD des associations entre utilisateurs et projets.
 */
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberServiceImpl(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    @Override
    public List<ProjectMember> findAll() {
        List<ProjectMember> projectMembers = new ArrayList<>();
        projectMemberRepository.findAll().forEach(projectMembers::add);
        return projectMembers;
    }

    @Override
    public ProjectMember findById(Long id) {
        Optional<ProjectMember> projectMember = projectMemberRepository.findById(id);
        if (projectMember.isPresent()) {
            return projectMember.get();
        }
        throw new EntityDontExistException();
    }

    @Override
    public Long create(ProjectMember projectMember) {
        return projectMemberRepository.save(projectMember).getId();
    }

    @Override
    public ProjectMember updateRole(Long memberId, ProjectRole role, Long requestedById) {
        ProjectMember projectMember = findById(memberId);
        ensureProjectAdmin(projectMember.getProject(), requestedById);

        Long ownerId = projectMember.getProject().getOwner() != null ? projectMember.getProject().getOwner().getId() : null;
        Long memberUserId = projectMember.getUser() != null ? projectMember.getUser().getId() : null;
        if (ownerId != null && ownerId.equals(memberUserId)) {
            throw new BadRequestException("Le proprietaire du projet conserve le role administrateur.");
        }

        projectMember.assignRole(role);
        return projectMemberRepository.save(projectMember);
    }

    @Override
    public void delete(ProjectMember projectMember) {
        projectMemberRepository.delete(projectMember);
    }

    private void ensureProjectAdmin(Project project, Long userId) {
        boolean isOwner = project.getOwner() != null && project.getOwner().getId() != null && project.getOwner().getId().equals(userId);
        boolean isAdminMember = projectMemberRepository.findByProjectIdAndUserIdAndRole(project.getId(), userId, ProjectRole.ADMIN).isPresent();

        if (!isOwner && !isAdminMember) {
            throw new BadRequestException("Seul un administrateur du projet peut modifier les roles.");
        }
    }
}

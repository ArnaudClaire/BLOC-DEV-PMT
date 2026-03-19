package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.ProjectMemberRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.ProjectMember;
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
    public void delete(ProjectMember projectMember) {
        projectMemberRepository.delete(projectMember);
    }
}

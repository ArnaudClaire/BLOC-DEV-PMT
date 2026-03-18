package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.ProjectInvitationRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.services.ProjectInvitationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectInvitationServiceImpl implements ProjectInvitationService {

    private final ProjectInvitationRepository projectInvitationRepository;

    public ProjectInvitationServiceImpl(ProjectInvitationRepository projectInvitationRepository) {
        this.projectInvitationRepository = projectInvitationRepository;
    }

    @Override
    public List<ProjectInvitation> findAll() {
        List<ProjectInvitation> invitations = new ArrayList<>();
        projectInvitationRepository.findAll().forEach(invitations::add);
        return invitations;
    }

    @Override
    public ProjectInvitation findById(Long id) {
        Optional<ProjectInvitation> projectInvitation = projectInvitationRepository.findById(id);
        if (projectInvitation.isPresent()) {
            return projectInvitation.get();
        }
        throw new EntityDontExistException();
    }

    @Override
    public Long create(ProjectInvitation projectInvitation) {
        return projectInvitationRepository.save(projectInvitation).getId();
    }

    @Override
    public void delete(ProjectInvitation projectInvitation) {
        projectInvitationRepository.delete(projectInvitation);
    }
}

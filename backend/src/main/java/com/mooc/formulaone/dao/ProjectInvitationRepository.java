package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.InvitationStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data pour les invitations de projet.
 */
public interface ProjectInvitationRepository extends CrudRepository<ProjectInvitation, Long> {
    Optional<ProjectInvitation> findByToken(String token);

    List<ProjectInvitation> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    boolean existsByProjectIdAndEmailIgnoreCaseAndStatus(Long projectId, String email, InvitationStatus status);
}

package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.ProjectInvitationRepository;
import com.mooc.formulaone.dao.ProjectMemberRepository;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.InvitationStatus;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.ProjectInvitationEmailService;
import com.mooc.formulaone.services.ProjectInvitationService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
/**
 * Implémente le flux métier complet des invitations de projet.
 */
public class ProjectInvitationServiceImpl implements ProjectInvitationService {

    private final ProjectInvitationRepository projectInvitationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectInvitationEmailService projectInvitationEmailService;

    public ProjectInvitationServiceImpl(
            ProjectInvitationRepository projectInvitationRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectInvitationEmailService projectInvitationEmailService
    ) {
        this.projectInvitationRepository = projectInvitationRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectInvitationEmailService = projectInvitationEmailService;
    }

    @Override
    /**
     * Retourne toutes les invitations après mise à jour éventuelle de leur expiration.
     *
     * @return l'ensemble des invitations connues
     */
    public List<ProjectInvitation> findAll() {
        List<ProjectInvitation> invitations = new ArrayList<>();
        projectInvitationRepository.findAll().forEach(invitations::add);
        invitations.forEach(this::refreshExpirationIfNeeded);
        return invitations;
    }

    @Override
    /**
     * Recherche une invitation par identifiant.
     *
     * @param id identifiant technique
     * @return l'invitation correspondante
     */
    public ProjectInvitation findById(Long id) {
        Optional<ProjectInvitation> projectInvitation = projectInvitationRepository.findById(id);
        if (projectInvitation.isPresent()) {
            return refreshExpirationIfNeeded(projectInvitation.get());
        }
        throw new EntityDontExistException();
    }

    @Override
    /**
     * Liste les invitations d'un projet.
     *
     * @param projectId identifiant du projet
     * @return les invitations du projet
     */
    public List<ProjectInvitation> findByProjectId(Long projectId) {
        List<ProjectInvitation> invitations = projectInvitationRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
        invitations.forEach(this::refreshExpirationIfNeeded);
        return invitations;
    }

    @Override
    /**
     * Résout une invitation à partir de son token public.
     *
     * @param token token transmis dans le lien d'invitation
     * @return l'invitation correspondante
     */
    public ProjectInvitation findByToken(String token) {
        ProjectInvitation invitation = projectInvitationRepository.findByToken(token).orElseThrow(EntityDontExistException::new);
        return refreshExpirationIfNeeded(invitation);
    }

    @Override
    /**
     * Crée une invitation, génère son token et déclenche l'envoi d'email.
     *
     * @param projectInvitation invitation à enregistrer
     * @return identifiant généré
     */
    public Long create(ProjectInvitation projectInvitation) {
        validateInvitationCreation(projectInvitation);

        String normalizedEmail = normalizeEmail(projectInvitation.getEmail());
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(Duration.ofDays(7)));

        projectInvitation.setEmail(normalizedEmail);
        projectInvitation.sendInvitation(UUID.randomUUID().toString(), expiresAt);

        ProjectInvitation savedInvitation = projectInvitationRepository.save(projectInvitation);
        projectInvitationEmailService.sendInvitation(savedInvitation);
        return savedInvitation.getId();
    }

    @Override
    /**
     * Accepte une invitation pour l'utilisateur fourni.
     *
     * @param token token d'invitation
     * @param user utilisateur qui accepte l'invitation
     * @return invitation mise à jour
     */
    public ProjectInvitation accept(String token, User user) {
        ProjectInvitation invitation = findByToken(token);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Cette invitation ne peut plus etre acceptee.");
        }

        if (!normalizeEmail(user.getEmail()).equals(normalizeEmail(invitation.getEmail()))) {
            throw new BadRequestException("Cette invitation est reservee a une autre adresse email.");
        }

        if (projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(invitation.getProject().getId(), user.getEmail())) {
            throw new BadRequestException("Cet utilisateur fait deja partie du projet.");
        }

        ProjectMember projectMember = new ProjectMember();
        projectMember.setRole(invitation.getRole());
        projectMember.setJoinedAt(Timestamp.from(Instant.now()));
        projectMember.setProject(invitation.getProject());
        projectMember.setUser(user);
        projectMemberRepository.save(projectMember);

        invitation.accept(user, Timestamp.from(Instant.now()));
        return projectInvitationRepository.save(invitation);
    }

    @Override
    /**
     * Annule une invitation encore en attente.
     *
     * @param invitationId identifiant de l'invitation
     * @param adminUserId identifiant de l'administrateur demandeur
     * @return invitation annulée
     */
    public ProjectInvitation cancel(Long invitationId, Long adminUserId) {
        ProjectInvitation invitation = findById(invitationId);
        ensureProjectAdmin(invitation.getProject(), adminUserId);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Seules les invitations en attente peuvent etre annulees.");
        }

        invitation.cancel(Timestamp.from(Instant.now()));
        return projectInvitationRepository.save(invitation);
    }

    @Override
    /**
     * Renvoie une invitation existante avec un nouveau token et une nouvelle expiration.
     *
     * @param invitationId identifiant de l'invitation
     * @param adminUserId identifiant de l'administrateur demandeur
     * @return invitation renvoyée
     */
    public ProjectInvitation resend(Long invitationId, Long adminUserId) {
        ProjectInvitation invitation = findById(invitationId);
        ensureProjectAdmin(invitation.getProject(), adminUserId);

        if (invitation.getStatus() == InvitationStatus.ACCEPTED) {
            throw new BadRequestException("Une invitation deja acceptee ne peut pas etre renvoyee.");
        }

        if (projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(invitation.getProject().getId(), invitation.getEmail())) {
            throw new BadRequestException("Cette adresse email appartient deja a un membre du projet.");
        }

        invitation.sendInvitation(UUID.randomUUID().toString(), Timestamp.from(Instant.now().plus(Duration.ofDays(7))));
        ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);
        projectInvitationEmailService.sendInvitation(savedInvitation);
        return savedInvitation;
    }

    @Override
    /**
     * Supprime définitivement une invitation.
     *
     * @param projectInvitation invitation à supprimer
     */
    public void delete(ProjectInvitation projectInvitation) {
        projectInvitationRepository.delete(projectInvitation);
    }

    /**
     * Vérifie qu'une invitation peut être créée pour le projet et l'adresse demandés.
     *
     * @param projectInvitation invitation en cours de création
     */
    private void validateInvitationCreation(ProjectInvitation projectInvitation) {
        if (projectInvitation.getProject() == null || projectInvitation.getProject().getId() == null) {
            throw new BadRequestException("Le projet cible est obligatoire.");
        }

        if (projectInvitation.getInvitedBy() == null || projectInvitation.getInvitedBy().getId() == null) {
            throw new BadRequestException("L'administrateur qui invite est obligatoire.");
        }

        ensureProjectAdmin(projectInvitation.getProject(), projectInvitation.getInvitedBy().getId());

        String normalizedEmail = normalizeEmail(projectInvitation.getEmail());
        if (projectMemberRepository.existsByProjectIdAndUserEmailIgnoreCase(projectInvitation.getProject().getId(), normalizedEmail)) {
            throw new BadRequestException("Cette adresse email fait deja partie du projet.");
        }

        if (projectInvitationRepository.existsByProjectIdAndEmailIgnoreCaseAndStatus(
                projectInvitation.getProject().getId(),
                normalizedEmail,
                InvitationStatus.PENDING
        )) {
            throw new BadRequestException("Une invitation active existe deja pour cette adresse email.");
        }
    }

    /**
     * Contrôle que l'utilisateur courant dispose d'un rôle administrateur sur le projet.
     *
     * @param project projet concerné par l'action
     * @param userId identifiant de l'utilisateur à vérifier
     */
    private void ensureProjectAdmin(Project project, Long userId) {
        boolean isOwner = project.getOwner() != null && project.getOwner().getId() != null && project.getOwner().getId().equals(userId);
        boolean isAdminMember = projectMemberRepository.findByProjectIdAndUserIdAndRole(project.getId(), userId, ProjectRole.ADMIN).isPresent();

        if (!isOwner && !isAdminMember) {
            throw new BadRequestException("Seul un administrateur du projet peut effectuer cette action.");
        }
    }

    /**
     * Expire automatiquement une invitation encore en attente lorsqu'elle a dépassé sa date limite.
     *
     * @param invitation invitation à contrôler
     * @return l'invitation mise à jour si son statut a changé
     */
    private ProjectInvitation refreshExpirationIfNeeded(ProjectInvitation invitation) {
        if (invitation.getStatus() == InvitationStatus.PENDING
                && invitation.getExpiresAt() != null
                && invitation.getExpiresAt().before(Timestamp.from(Instant.now()))) {
            invitation.expire();
            return projectInvitationRepository.save(invitation);
        }

        return invitation;
    }

    /**
     * Normalise une adresse email pour fiabiliser les comparaisons métier.
     *
     * @param email adresse brute reçue depuis le front ou la base
     * @return l'adresse nettoyée en minuscules
     */
    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}

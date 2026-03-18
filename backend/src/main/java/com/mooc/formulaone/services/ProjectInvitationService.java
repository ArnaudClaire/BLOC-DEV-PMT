package com.mooc.formulaone.services;

import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.models.User;

import java.util.List;

public interface ProjectInvitationService {

    /**
     * Retourne toutes les invitations de projet.
     *
     * @return la liste des invitations
     */
    List<ProjectInvitation> findAll();

    /**
     * Recherche une invitation par identifiant.
     *
     * @param id identifiant de l'invitation
     * @return l'invitation correspondante
     */
    ProjectInvitation findById(Long id);

    /**
     * Retourne toutes les invitations d'un projet.
     *
     * @param projectId identifiant du projet
     * @return la liste triee des invitations du projet
     */
    List<ProjectInvitation> findByProjectId(Long projectId);

    /**
     * Recherche une invitation via son token public.
     *
     * @param token token securise de l'invitation
     * @return l'invitation correspondante
     */
    ProjectInvitation findByToken(String token);

    /**
     * Cree une nouvelle invitation de projet.
     *
     * @param projectInvitation invitation a enregistrer
     * @return identifiant genere
     */
    Long create(ProjectInvitation projectInvitation);

    /**
     * Accepte une invitation de projet pour l'utilisateur donne.
     *
     * @param token token de l'invitation
     * @param user utilisateur qui accepte
     * @return l'invitation mise a jour
     */
    ProjectInvitation accept(String token, User user);

    /**
     * Annule une invitation existante.
     *
     * @param invitationId identifiant de l'invitation
     * @param adminUserId identifiant de l'administrateur qui annule
     * @return l'invitation annulee
     */
    ProjectInvitation cancel(Long invitationId, Long adminUserId);

    /**
     * Renvoie une invitation existante.
     *
     * @param invitationId identifiant de l'invitation
     * @param adminUserId identifiant de l'administrateur demandeur
     * @return l'invitation remise en attente
     */
    ProjectInvitation resend(Long invitationId, Long adminUserId);

    /**
     * Supprime une invitation existante.
     *
     * @param projectInvitation invitation a supprimer
     */
    void delete(ProjectInvitation projectInvitation);
}

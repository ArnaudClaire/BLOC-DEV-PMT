package com.mooc.formulaone.services;

import com.mooc.formulaone.models.ProjectInvitation;

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
     * Cree une nouvelle invitation de projet.
     *
     * @param projectInvitation invitation a enregistrer
     * @return identifiant genere
     */
    Long create(ProjectInvitation projectInvitation);

    /**
     * Supprime une invitation existante.
     *
     * @param projectInvitation invitation a supprimer
     */
    void delete(ProjectInvitation projectInvitation);
}

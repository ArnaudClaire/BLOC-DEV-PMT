package com.mooc.formulaone.services;

import com.mooc.formulaone.models.ProjectMember;

import java.util.List;

public interface ProjectMemberService {

    /**
     * Retourne toutes les associations utilisateur/projet.
     *
     * @return la liste des membres de projets
     */
    List<ProjectMember> findAll();

    /**
     * Recherche une association membre par identifiant.
     *
     * @param id identifiant technique
     * @return le membre de projet correspondant
     */
    ProjectMember findById(Long id);

    /**
     * Cree une nouvelle association membre.
     *
     * @param projectMember lien utilisateur/projet a enregistrer
     * @return identifiant genere
     */
    Long create(ProjectMember projectMember);

    /**
     * Supprime une association membre existante.
     *
     * @param projectMember association a supprimer
     */
    void delete(ProjectMember projectMember);
}

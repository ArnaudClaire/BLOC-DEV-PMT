package com.mooc.formulaone.services;

import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;

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
     * Met a jour le role d'un membre de projet existant.
     *
     * @param memberId identifiant du membre de projet
     * @param role nouveau role a appliquer
     * @param requestedById identifiant de l'administrateur a l'origine du changement
     * @return l'association mise a jour
     */
    ProjectMember updateRole(Long memberId, ProjectRole role, Long requestedById);

    /**
     * Supprime une association membre existante.
     *
     * @param projectMember association a supprimer
     */
    void delete(ProjectMember projectMember);
}

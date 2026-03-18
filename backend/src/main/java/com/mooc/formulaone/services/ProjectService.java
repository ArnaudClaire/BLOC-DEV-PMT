package com.mooc.formulaone.services;

import com.mooc.formulaone.models.Project;

import java.util.List;

public interface ProjectService {

    /**
     * Retourne l'ensemble des projets.
     *
     * @return la liste des projets
     */
    List<Project> findAll();

    /**
     * Recherche un projet par identifiant.
     *
     * @param id identifiant du projet
     * @return le projet correspondant
     */
    Project findById(Long id);

    /**
     * Cree un projet.
     *
     * @param project projet a enregistrer
     * @return identifiant genere
     */
    Long create(Project project);

    /**
     * Supprime un projet existant.
     *
     * @param project projet a supprimer
     */
    void delete(Project project);
}

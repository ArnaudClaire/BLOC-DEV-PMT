package com.mooc.formulaone.services;

import com.mooc.formulaone.models.Task;

import java.util.List;

public interface TaskService {

    /**
     * Retourne toutes les taches du systeme.
     *
     * @return la liste des taches
     */
    List<Task> findAll();

    /**
     * Recherche une tache par identifiant.
     *
     * @param id identifiant de la tache
     * @return la tache correspondante
     */
    Task findById(Long id);

    /**
     * Cree une nouvelle tache.
     *
     * @param task tache a enregistrer
     * @return identifiant genere
     */
    Long create(Task task);

    /**
     * Supprime une tache existante.
     *
     * @param task tache a supprimer
     */
    void delete(Task task);
}

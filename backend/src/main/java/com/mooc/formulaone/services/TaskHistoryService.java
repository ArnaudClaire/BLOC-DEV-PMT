package com.mooc.formulaone.services;

import com.mooc.formulaone.models.TaskHistory;

import java.util.List;

public interface TaskHistoryService {

    /**
     * Retourne tout l'historique des taches.
     *
     * @return la liste des entrees d'historique
     */
    List<TaskHistory> findAll();

    /**
     * Recherche une entree d'historique par identifiant.
     *
     * @param id identifiant de l'historique
     * @return l'entree correspondante
     */
    TaskHistory findById(Long id);

    /**
     * Cree une entree d'historique.
     *
     * @param taskHistory entree a enregistrer
     * @return identifiant genere
     */
    Long create(TaskHistory taskHistory);

    /**
     * Supprime une entree d'historique existante.
     *
     * @param taskHistory entree a supprimer
     */
    void delete(TaskHistory taskHistory);
}

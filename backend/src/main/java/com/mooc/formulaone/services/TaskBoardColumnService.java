package com.mooc.formulaone.services;

import com.mooc.formulaone.models.TaskBoardColumn;

import java.util.List;

/**
 * Contrat de service pour la gestion des colonnes du tableau Kanban.
 */
public interface TaskBoardColumnService {

    /**
     * Retourne toutes les colonnes connues.
     *
     * @return la liste complète des colonnes
     */
    List<TaskBoardColumn> findAll();

    /**
     * Retourne les colonnes d'un projet donné.
     *
     * @param projectId identifiant du projet
     * @return les colonnes rattachées au projet
     */
    List<TaskBoardColumn> findByProjectId(Long projectId);

    /**
     * Recherche une colonne par identifiant.
     *
     * @param id identifiant technique
     * @return la colonne correspondante
     */
    TaskBoardColumn findById(Long id);

    /**
     * Persiste une nouvelle colonne.
     *
     * @param taskBoardColumn colonne à enregistrer
     * @return identifiant généré
     */
    Long create(TaskBoardColumn taskBoardColumn);
}

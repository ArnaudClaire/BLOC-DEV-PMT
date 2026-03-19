package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.TaskBoardColumn;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository Spring Data pour les colonnes de tableau Kanban.
 */
public interface TaskBoardColumnRepository extends CrudRepository<TaskBoardColumn, Long> {

    List<TaskBoardColumn> findByProjectIdOrderByDisplayOrderAsc(Long projectId);
}

package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.TaskHistory;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository Spring Data pour les historiques de tâches.
 */
public interface TaskHistoryRepository extends CrudRepository<TaskHistory, Long> {
}

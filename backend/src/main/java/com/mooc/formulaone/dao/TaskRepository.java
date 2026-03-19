package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Task;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository Spring Data pour les tâches.
 */
public interface TaskRepository extends CrudRepository<Task, Long> {
}

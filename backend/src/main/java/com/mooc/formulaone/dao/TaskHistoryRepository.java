package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.TaskHistory;
import org.springframework.data.repository.CrudRepository;

public interface TaskHistoryRepository extends CrudRepository<TaskHistory, Long> {
}

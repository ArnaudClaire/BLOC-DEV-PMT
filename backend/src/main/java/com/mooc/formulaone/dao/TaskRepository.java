package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {
}

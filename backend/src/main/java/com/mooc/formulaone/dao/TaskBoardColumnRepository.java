package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.TaskBoardColumn;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskBoardColumnRepository extends CrudRepository<TaskBoardColumn, Long> {

    List<TaskBoardColumn> findByProjectIdOrderByDisplayOrderAsc(Long projectId);
}

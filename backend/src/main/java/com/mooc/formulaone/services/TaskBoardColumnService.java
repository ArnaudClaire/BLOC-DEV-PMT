package com.mooc.formulaone.services;

import com.mooc.formulaone.models.TaskBoardColumn;

import java.util.List;

public interface TaskBoardColumnService {

    List<TaskBoardColumn> findAll();

    List<TaskBoardColumn> findByProjectId(Long projectId);

    TaskBoardColumn findById(Long id);

    Long create(TaskBoardColumn taskBoardColumn);
}

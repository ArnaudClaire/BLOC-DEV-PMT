package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.TaskBoardColumnRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.services.TaskBoardColumnService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implementation chargee de persister les colonnes d'etats des boards.
 */
public class TaskBoardColumnServiceImpl implements TaskBoardColumnService {

    private final TaskBoardColumnRepository taskBoardColumnRepository;

    public TaskBoardColumnServiceImpl(TaskBoardColumnRepository taskBoardColumnRepository) {
        this.taskBoardColumnRepository = taskBoardColumnRepository;
    }

    @Override
    public List<TaskBoardColumn> findAll() {
        List<TaskBoardColumn> columns = new ArrayList<>();
        taskBoardColumnRepository.findAll().forEach(columns::add);
        columns.sort(Comparator
                .comparing((TaskBoardColumn column) -> column.getProject() != null ? column.getProject().getId() : 0L)
                .thenComparing(column -> column.getDisplayOrder() != null ? column.getDisplayOrder() : Integer.MAX_VALUE)
                .thenComparing(TaskBoardColumn::getId));
        return columns;
    }

    @Override
    public List<TaskBoardColumn> findByProjectId(Long projectId) {
        return taskBoardColumnRepository.findByProjectIdOrderByDisplayOrderAsc(projectId);
    }

    @Override
    public TaskBoardColumn findById(Long id) {
        Optional<TaskBoardColumn> taskBoardColumn = taskBoardColumnRepository.findById(id);
        if (taskBoardColumn.isPresent()) {
            return taskBoardColumn.get();
        }
        throw new EntityDontExistException();
    }

    @Override
    public Long create(TaskBoardColumn taskBoardColumn) {
        return taskBoardColumnRepository.save(taskBoardColumn).getId();
    }
}

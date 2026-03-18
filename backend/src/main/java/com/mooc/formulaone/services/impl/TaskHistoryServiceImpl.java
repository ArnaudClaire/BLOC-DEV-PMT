package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.TaskHistoryRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.TaskHistory;
import com.mooc.formulaone.services.TaskHistoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskHistoryServiceImpl implements TaskHistoryService {

    private final TaskHistoryRepository taskHistoryRepository;

    public TaskHistoryServiceImpl(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @Override
    public List<TaskHistory> findAll() {
        List<TaskHistory> taskHistories = new ArrayList<>();
        taskHistoryRepository.findAll().forEach(taskHistories::add);
        return taskHistories;
    }

    @Override
    public TaskHistory findById(Long id) {
        Optional<TaskHistory> taskHistory = taskHistoryRepository.findById(id);
        if (taskHistory.isPresent()) {
            return taskHistory.get();
        }
        throw new EntityDontExistException();
    }

    @Override
    public Long create(TaskHistory taskHistory) {
        return taskHistoryRepository.save(taskHistory).getId();
    }

    @Override
    public void delete(TaskHistory taskHistory) {
        taskHistoryRepository.delete(taskHistory);
    }
}

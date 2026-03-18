package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskBoardColumnCreateRequest;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion des colonnes d'etats du board.
 */
public class TaskBoardColumnController {

    private final TaskBoardColumnService taskBoardColumnService;
    private final ProjectService projectService;

    public TaskBoardColumnController(
            TaskBoardColumnService taskBoardColumnService,
            ProjectService projectService
    ) {
        this.taskBoardColumnService = taskBoardColumnService;
        this.projectService = projectService;
    }

    @GetMapping("/task-board-columns")
    @ResponseStatus(code = HttpStatus.OK)
    public List<TaskBoardColumn> findAll() {
        return taskBoardColumnService.findAll();
    }

    @GetMapping("/projects/{projectId}/task-board-columns")
    @ResponseStatus(code = HttpStatus.OK)
    public List<TaskBoardColumn> findByProjectId(@PathVariable Long projectId) {
        return taskBoardColumnService.findByProjectId(projectId);
    }

    @GetMapping("/task-board-columns/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public TaskBoardColumn findById(@PathVariable Long id) {
        return taskBoardColumnService.findById(id);
    }

    @PostMapping("/task-board-columns")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long create(@Valid @RequestBody TaskBoardColumnCreateRequest request) {
        boolean alreadyExists = taskBoardColumnService.findByProjectId(request.projectId()).stream()
                .anyMatch(column -> column.getName().equalsIgnoreCase(request.name().trim()));
        if (alreadyExists) {
            throw new BadRequestException("Une colonne avec ce nom existe deja pour ce projet.");
        }

        TaskBoardColumn taskBoardColumn = new TaskBoardColumn();
        taskBoardColumn.setName(request.name().trim());
        taskBoardColumn.setProject(projectService.findById(request.projectId()));
        taskBoardColumn.setDisplayOrder(request.displayOrder() != null
                ? request.displayOrder()
                : taskBoardColumnService.findByProjectId(request.projectId()).size());
        return taskBoardColumnService.create(taskBoardColumn);
    }
}

package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskHistoryCreateRequest;
import com.mooc.formulaone.models.TaskHistory;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.TaskHistoryService;
import com.mooc.formulaone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion de l'historique des taches.
 */
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;
    private final TaskService taskService;
    private final UserService userService;

    public TaskHistoryController(
            TaskHistoryService taskHistoryService,
            TaskService taskService,
            UserService userService
    ) {
        this.taskHistoryService = taskHistoryService;
        this.taskService = taskService;
        this.userService = userService;
    }

    /**
     * Retourne toutes les entrees d'historique.
     *
     * @return la liste des historiques
     */
    @GetMapping("/task-histories")
    @ResponseStatus(code = HttpStatus.OK)
    public List<TaskHistory> findAll() {
        return taskHistoryService.findAll();
    }

    /**
     * Retourne une entree d'historique a partir de son identifiant.
     *
     * @param id identifiant de l'historique
     * @return l'entree correspondante
     */
    @GetMapping("/task-histories/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public TaskHistory findById(@PathVariable Long id) {
        return taskHistoryService.findById(id);
    }

    /**
     * Cree une entree d'historique.
     *
     * @param request entree a enregistrer
     * @return identifiant genere
     */
    @PostMapping("/task-histories")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long create(@Valid @RequestBody TaskHistoryCreateRequest request) {
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setActionType(request.actionType());
        taskHistory.setFieldName(request.fieldName());
        taskHistory.setOldValue(request.oldValue());
        taskHistory.setNewValue(request.newValue());
        taskHistory.setTask(taskService.findById(request.taskId()));
        taskHistory.setChangedBy(userService.findById(request.changedById()));
        return taskHistoryService.create(taskHistory);
    }

    /**
     * Supprime une entree d'historique existante.
     *
     * @param id identifiant de l'entree a supprimer
     */
    @DeleteMapping("/task-histories/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        TaskHistory taskHistory = taskHistoryService.findById(id);
        taskHistoryService.delete(taskHistory);
    }
}

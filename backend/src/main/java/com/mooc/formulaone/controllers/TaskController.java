package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskCreateRequest;
import com.mooc.formulaone.controllers.dto.TaskUpdateRequest;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion des taches.
 */
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final TaskBoardColumnService taskBoardColumnService;
    private final UserService userService;

    public TaskController(
            TaskService taskService,
            ProjectService projectService,
            TaskBoardColumnService taskBoardColumnService,
            UserService userService
    ) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.taskBoardColumnService = taskBoardColumnService;
        this.userService = userService;
    }

    /**
     * Retourne toutes les taches.
     *
     * @return la liste complete des taches
     */
    @GetMapping("/tasks")
    @ResponseStatus(code = HttpStatus.OK)
    public List<Task> findAll() {
        return taskService.findAll();
    }

    /**
     * Retourne une tache a partir de son identifiant.
     *
     * @param id identifiant de la tache
     * @return la tache correspondante
     */
    @GetMapping("/tasks/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public Task findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    /**
     * Cree une tache.
     *
     * @param request charge utile de creation
     * @return identifiant genere
     */
    @PostMapping("/tasks")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long create(@Valid @RequestBody TaskCreateRequest request) {
        validateTaskStatus(request.projectId(), request.status());
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setEndDate(request.endDate());
        task.setProject(projectService.findById(request.projectId()));
        task.setCreatedBy(userService.findById(request.createdById()));
        if (request.assignedToId() != null) {
            task.setAssignedTo(userService.findById(request.assignedToId()));
        }
        return taskService.create(task);
    }

    /**
     * Met a jour une tache existante.
     *
     * @param id identifiant de la tache
     * @param request charge utile de mise a jour
     */
    @PutMapping("/tasks/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        Task task = taskService.findById(id);
        validateTaskStatus(task.getProject().getId(), request.status());
        task.updateTask(
                request.title(),
                request.description(),
                request.status(),
                request.priority(),
                request.dueDate(),
                request.endDate()
        );

        if (request.assignedToId() != null) {
            task.assignTo(userService.findById(request.assignedToId()));
        } else {
            task.assignTo(null);
        }

        taskService.update(task);
    }

    private void validateTaskStatus(Long projectId, String status) {
        boolean statusExists = taskBoardColumnService.findByProjectId(projectId).stream()
                .anyMatch(column -> column.getName().equalsIgnoreCase(status));
        if (!statusExists) {
            throw new BadRequestException("Le statut demande n'existe pas pour ce projet.");
        }
    }

    /**
     * Supprime une tache existante.
     *
     * @param id identifiant de la tache a supprimer
     */
    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        Task task = taskService.findById(id);
        taskService.delete(task);
    }
}

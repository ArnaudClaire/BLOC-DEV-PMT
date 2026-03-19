package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskCreateRequest;
import com.mooc.formulaone.controllers.dto.TaskUpdateRequest;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Tâches",
        description = "Gestion des tâches PMT : consultation, création, mise à jour, suppression et validation du statut par projet."
)
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
    @Operation(summary = "Lister les tâches", description = "Retourne toutes les tâches connues par le backend.")
    @ApiResponse(responseCode = "200", description = "Liste des tâches récupérée avec succès.")
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
    @Operation(summary = "Récupérer une tâche", description = "Retourne une tâche précise à partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tâche trouvée."),
            @ApiResponse(responseCode = "404", description = "Tâche introuvable.")
    })
    public Task findById(
            @Parameter(description = "Identifiant de la tâche.", example = "42")
            @PathVariable Long id
    ) {
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
    @Operation(
            summary = "Créer une tâche",
            description = "Crée une tâche dans un projet donné après validation du statut demandé par rapport aux colonnes configurées pour le board."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations nécessaires à la création d'une tâche.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Tâche assignée",
                    value = """
                            {
                              "title": "Préparer la démonstration",
                              "description": "Assembler les captures et les scénarios de présentation.",
                              "status": "TODO",
                              "priority": "HIGH",
                              "dueDate": "2026-03-25",
                              "endDate": "2026-03-25",
                              "projectId": 12,
                              "createdById": 1,
                              "assignedToId": 4
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tâche créée."),
            @ApiResponse(responseCode = "400", description = "Données invalides ou statut non autorisé."),
            @ApiResponse(responseCode = "404", description = "Projet ou utilisateur introuvable.")
    })
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
    @Operation(
            summary = "Mettre à jour une tâche",
            description = "Met à jour les champs éditables d'une tâche existante, y compris le statut, la priorité, les dates et l'assignation."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nouvel état de la tâche à persister.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Passage en cours",
                    value = """
                            {
                              "title": "Préparer la démonstration",
                              "description": "Scénario finalisé et relu.",
                              "status": "IN_PROGRESS",
                              "priority": "HIGH",
                              "dueDate": "2026-03-25",
                              "endDate": "2026-03-24",
                              "assignedToId": 4
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tâche mise à jour."),
            @ApiResponse(responseCode = "400", description = "Données invalides ou statut non autorisé."),
            @ApiResponse(responseCode = "404", description = "Tâche ou utilisateur introuvable.")
    })
    public void update(
            @Parameter(description = "Identifiant de la tâche à modifier.", example = "42")
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
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
    @Operation(summary = "Supprimer une tâche", description = "Supprime définitivement une tâche existante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tâche supprimée."),
            @ApiResponse(responseCode = "404", description = "Tâche introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant de la tâche à supprimer.", example = "42")
            @PathVariable Long id
    ) {
        Task task = taskService.findById(id);
        taskService.delete(task);
    }
}

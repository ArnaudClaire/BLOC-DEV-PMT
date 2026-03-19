package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskHistoryCreateRequest;
import com.mooc.formulaone.models.TaskHistory;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.TaskHistoryService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion de l'historique des taches.
 */
@Tag(
        name = "Historique des tâches",
        description = "Journal des modifications apportées aux tâches : création, assignation, changement de statut et mises à jour."
)
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
    @Operation(summary = "Lister les historiques", description = "Retourne toutes les entrées d'historique de tâches.")
    @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès.")
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
    @Operation(summary = "Récupérer une entrée d'historique", description = "Retourne une entrée d'historique à partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée trouvée."),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable.")
    })
    public TaskHistory findById(
            @Parameter(description = "Identifiant de l'entrée d'historique.", example = "15")
            @PathVariable Long id
    ) {
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
    @Operation(
            summary = "Créer une entrée d'historique",
            description = "Ajoute une entrée d'historique pour tracer une modification métier sur une tâche."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Événement métier à enregistrer dans l'historique.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Changement de statut",
                    value = """
                            {
                              "actionType": "STATUS_CHANGED",
                              "fieldName": "status",
                              "oldValue": "TODO",
                              "newValue": "IN_PROGRESS",
                              "taskId": 42,
                              "changedById": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Historique créé."),
            @ApiResponse(responseCode = "400", description = "Données invalides."),
            @ApiResponse(responseCode = "404", description = "Tâche ou utilisateur introuvable.")
    })
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
    @Operation(summary = "Supprimer une entrée d'historique", description = "Supprime une entrée d'historique existante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrée supprimée."),
            @ApiResponse(responseCode = "404", description = "Entrée introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant de l'entrée d'historique à supprimer.", example = "15")
            @PathVariable Long id
    ) {
        TaskHistory taskHistory = taskHistoryService.findById(id);
        taskHistoryService.delete(taskHistory);
    }
}

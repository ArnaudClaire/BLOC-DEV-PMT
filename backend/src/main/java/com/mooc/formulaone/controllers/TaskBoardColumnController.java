package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.TaskBoardColumnCreateRequest;
import com.mooc.formulaone.exceptions.BadRequestException;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Colonnes Kanban",
        description = "Configuration des colonnes de statut disponibles sur le board d'un projet."
)
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
    @Operation(summary = "Lister toutes les colonnes", description = "Retourne toutes les colonnes de board connues par le backend.")
    @ApiResponse(responseCode = "200", description = "Colonnes récupérées avec succès.")
    public List<TaskBoardColumn> findAll() {
        return taskBoardColumnService.findAll();
    }

    @GetMapping("/projects/{projectId}/task-board-columns")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Lister les colonnes d'un projet", description = "Retourne les colonnes de statut configurées pour un projet donné.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colonnes du projet récupérées."),
            @ApiResponse(responseCode = "404", description = "Projet introuvable.")
    })
    public List<TaskBoardColumn> findByProjectId(
            @Parameter(description = "Identifiant du projet.", example = "12")
            @PathVariable Long projectId
    ) {
        return taskBoardColumnService.findByProjectId(projectId);
    }

    @GetMapping("/task-board-columns/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Récupérer une colonne", description = "Retourne une colonne de board à partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colonne trouvée."),
            @ApiResponse(responseCode = "404", description = "Colonne introuvable.")
    })
    public TaskBoardColumn findById(
            @Parameter(description = "Identifiant de la colonne.", example = "4")
            @PathVariable Long id
    ) {
        return taskBoardColumnService.findById(id);
    }

    @PostMapping("/task-board-columns")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
            summary = "Créer une colonne de board",
            description = "Ajoute une nouvelle colonne de statut à un projet après vérification d'unicité du nom."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Définition de la colonne à créer.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Nouvelle colonne",
                    value = """
                            {
                              "name": "REVIEW",
                              "displayOrder": 3,
                              "projectId": 12
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Colonne créée."),
            @ApiResponse(responseCode = "400", description = "Nom déjà utilisé ou charge utile invalide."),
            @ApiResponse(responseCode = "404", description = "Projet introuvable.")
    })
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

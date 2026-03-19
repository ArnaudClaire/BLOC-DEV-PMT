package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectCreateRequest;
import com.mooc.formulaone.bootstrap.TaskBoardColumnBootstrap;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.services.ProjectMemberService;
import com.mooc.formulaone.services.ProjectService;
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
 * Expose les endpoints REST de gestion des projets.
 */
@Tag(
        name = "Projets",
        description = "Gestion des projets PMT : consultation, création et suppression des espaces de travail."
)
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final TaskBoardColumnBootstrap taskBoardColumnBootstrap;
    private final UserService userService;

    public ProjectController(
            ProjectService projectService,
            ProjectMemberService projectMemberService,
            TaskBoardColumnBootstrap taskBoardColumnBootstrap,
            UserService userService
    ) {
        this.projectService = projectService;
        this.projectMemberService = projectMemberService;
        this.taskBoardColumnBootstrap = taskBoardColumnBootstrap;
        this.userService = userService;
    }

    /**
     * Retourne la liste des projets.
     *
     * @return les projets persistants
     */
    @GetMapping("/projects")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Lister les projets",
            description = "Retourne tous les projets persistés. Le frontend filtre ensuite ceux visibles pour l'utilisateur connecté."
    )
    @ApiResponse(responseCode = "200", description = "Liste des projets récupérée avec succès.")
    public List<Project> findAll() {
        return projectService.findAll();
    }

    /**
     * Retourne un projet a partir de son identifiant.
     *
     * @param id identifiant du projet
     * @return le projet correspondant
     */
    @GetMapping("/projects/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Récupérer un projet",
            description = "Retourne le détail d'un projet à partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projet trouvé."),
            @ApiResponse(responseCode = "404", description = "Projet introuvable.")
    })
    public Project findById(
            @Parameter(description = "Identifiant du projet à consulter.", example = "12")
            @PathVariable Long id
    ) {
        return projectService.findById(id);
    }

    /**
     * Cree un projet.
     *
     * @param request projet a enregistrer
     * @return identifiant genere
     */
    @PostMapping("/projects")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
            summary = "Créer un projet",
            description = "Crée un projet, enregistre automatiquement le propriétaire comme membre ADMIN puis initialise les colonnes Kanban par défaut."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Informations métier nécessaires à la création du projet.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Projet PMT",
                    value = """
                            {
                              "name": "Projet PMT",
                              "description": "Outil de gestion de projet partagé.",
                              "startDate": "2026-03-19",
                              "ownerId": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projet créé et initialisé."),
            @ApiResponse(responseCode = "400", description = "Données invalides."),
            @ApiResponse(responseCode = "404", description = "Propriétaire introuvable.")
    })
    public Long create(@Valid
                       @RequestBody
            ProjectCreateRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setStartDate(request.startDate());
        project.setOwner(userService.findById(request.ownerId()));
        Long projectId = projectService.create(project);

        ProjectMember ownerMembership = new ProjectMember();
        ownerMembership.setRole(ProjectRole.ADMIN);
        ownerMembership.setJoinedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        ownerMembership.setProject(projectService.findById(projectId));
        ownerMembership.setUser(userService.findById(request.ownerId()));
        projectMemberService.create(ownerMembership);
        taskBoardColumnBootstrap.createDefaultColumns(projectService.findById(projectId));

        return projectId;
    }

    /**
     * Supprime un projet s'il existe.
     *
     * @param id identifiant du projet a supprimer
     */
    @DeleteMapping("/projects/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Supprimer un projet",
            description = "Supprime un projet existant à partir de son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projet supprimé."),
            @ApiResponse(responseCode = "404", description = "Projet introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant du projet à supprimer.", example = "12")
            @PathVariable Long id
    ) {
        Project project = projectService.findById(id);
        projectService.delete(project);
    }
}

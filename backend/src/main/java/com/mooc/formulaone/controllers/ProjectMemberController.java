package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectMemberCreateRequest;
import com.mooc.formulaone.controllers.dto.ProjectMemberUpdateRequest;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.ProjectMemberService;
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
import java.sql.Timestamp;

@RestController
/**
 * Expose les endpoints REST de gestion des membres de projet.
 */
@Tag(
        name = "Membres de projet",
        description = "Gestion des rattachements entre utilisateurs et projets avec leurs rôles associés."
)
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final ProjectService projectService;
    private final UserService userService;

    public ProjectMemberController(
            ProjectMemberService projectMemberService,
            ProjectService projectService,
            UserService userService
    ) {
        this.projectMemberService = projectMemberService;
        this.projectService = projectService;
        this.userService = userService;
    }

    /**
     * Retourne toutes les associations utilisateur/projet.
     *
     * @return la liste des membres de projets
     */
    @GetMapping("/project-members")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Lister les membres de projet", description = "Retourne toutes les associations projet/utilisateur connues par le backend.")
    @ApiResponse(responseCode = "200", description = "Membres récupérés avec succès.")
    public List<ProjectMember> findAll() {
        return projectMemberService.findAll();
    }

    /**
     * Retourne une association membre par identifiant.
     *
     * @param id identifiant technique du membre
     * @return l'association correspondante
     */
    @GetMapping("/project-members/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Récupérer un membre de projet", description = "Retourne une association projet/utilisateur à partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association trouvée."),
            @ApiResponse(responseCode = "404", description = "Association introuvable.")
    })
    public ProjectMember findById(
            @Parameter(description = "Identifiant de l'association membre.", example = "5")
            @PathVariable Long id
    ) {
        return projectMemberService.findById(id);
    }

    /**
     * Cree une association entre un utilisateur et un projet.
     *
     * @param request charge utile de creation
     * @return identifiant genere
     */
    @PostMapping("/project-members")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
            summary = "Créer un membre de projet",
            description = "Rattache un utilisateur à un projet avec un rôle donné. Cet endpoint reste utile pour les scénarios internes ou les migrations."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Association projet/utilisateur à créer.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Ajout manuel",
                    value = """
                            {
                              "role": "MEMBER",
                              "joinedAt": "2026-03-19T09:00:00Z",
                              "projectId": 12,
                              "userId": 4
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membre créé."),
            @ApiResponse(responseCode = "400", description = "Données invalides."),
            @ApiResponse(responseCode = "404", description = "Projet ou utilisateur introuvable.")
    })
    public Long create(@Valid @RequestBody ProjectMemberCreateRequest request) {
        ProjectMember projectMember = new ProjectMember();
        projectMember.setRole(request.role());
        if (request.joinedAt() != null) {
            projectMember.setJoinedAt(Timestamp.from(request.joinedAt()));
        }
        projectMember.setProject(projectService.findById(request.projectId()));
        projectMember.setUser(userService.findById(request.userId()));
        return projectMemberService.create(projectMember);
    }

    /**
     * Met a jour le role d'un membre de projet existant.
     *
     * @param id identifiant de l'association a modifier
     * @param request nouveau role et administrateur demandeur
     */
    @PutMapping("/project-members/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Modifier le role d'un membre",
            description = "Permet a un administrateur de projet de modifier le role attribue a un membre deja rattache."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nouveau role a appliquer et identifiant de l'administrateur qui realise l'action.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Passage en observateur",
                    value = """
                            {
                              "role": "OBSERVER",
                              "requestedById": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role mis a jour."),
            @ApiResponse(responseCode = "400", description = "Modification non autorisee ou invalide."),
            @ApiResponse(responseCode = "404", description = "Association introuvable.")
    })
    public void updateRole(
            @Parameter(description = "Identifiant de l'association membre.", example = "5")
            @PathVariable Long id,
            @Valid @RequestBody ProjectMemberUpdateRequest request
    ) {
        projectMemberService.updateRole(id, request.role(), request.requestedById());
    }

    /**
     * Supprime une association membre existante.
     *
     * @param id identifiant de l'association a supprimer
     */
    @DeleteMapping("/project-members/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Supprimer un membre de projet", description = "Supprime une association existante entre un utilisateur et un projet.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Association supprimée."),
            @ApiResponse(responseCode = "404", description = "Association introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant de l'association à supprimer.", example = "5")
            @PathVariable Long id
    ) {
        ProjectMember projectMember = projectMemberService.findById(id);
        projectMemberService.delete(projectMember);
    }
}

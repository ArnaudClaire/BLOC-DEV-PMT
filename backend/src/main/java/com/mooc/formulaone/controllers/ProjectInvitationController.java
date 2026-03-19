package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectInvitationAcceptRequest;
import com.mooc.formulaone.controllers.dto.ProjectInvitationAcceptResponse;
import com.mooc.formulaone.controllers.dto.ProjectInvitationActionRequest;
import com.mooc.formulaone.controllers.dto.ProjectInvitationCreateRequest;
import com.mooc.formulaone.controllers.dto.ProjectInvitationPublicResponse;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.services.ProjectInvitationService;
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
 * Expose les endpoints REST de gestion des invitations de projet.
 */
@Tag(
        name = "Invitations de projet",
        description = "Création et gestion du flux d'invitation par email, y compris la résolution par token et l'acceptation."
)
public class ProjectInvitationController {

    private final ProjectInvitationService projectInvitationService;
    private final ProjectService projectService;
    private final UserService userService;

    public ProjectInvitationController(
            ProjectInvitationService projectInvitationService,
            ProjectService projectService,
            UserService userService
    ) {
        this.projectInvitationService = projectInvitationService;
        this.projectService = projectService;
        this.userService = userService;
    }

    /**
     * Retourne toutes les invitations de projet.
     *
     * @return la liste des invitations
     */
    @GetMapping("/project-invitations")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Lister toutes les invitations",
            description = "Retourne l'ensemble des invitations de projet connues par le backend."
    )
    @ApiResponse(responseCode = "200", description = "Invitations récupérées avec succès.")
    public List<ProjectInvitation> findAll() {
        return projectInvitationService.findAll();
    }

    /**
     * Retourne une invitation a partir de son identifiant.
     *
     * @param id identifiant de l'invitation
     * @return l'invitation correspondante
     */
    @GetMapping("/project-invitations/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Récupérer une invitation",
            description = "Retourne une invitation de projet à partir de son identifiant interne."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation trouvée."),
            @ApiResponse(responseCode = "404", description = "Invitation introuvable.")
    })
    public ProjectInvitation findById(
            @Parameter(description = "Identifiant interne de l'invitation.", example = "8")
            @PathVariable Long id
    ) {
        return projectInvitationService.findById(id);
    }

    /**
     * Retourne les invitations rattachees a un projet.
     *
     * @param projectId identifiant du projet
     * @return les invitations du projet
     */
    @GetMapping("/projects/{projectId}/project-invitations")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Lister les invitations d'un projet",
            description = "Retourne les invitations rattachées à un projet donné, classées côté service par date de création décroissante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitations du projet récupérées."),
            @ApiResponse(responseCode = "404", description = "Projet introuvable.")
    })
    public List<ProjectInvitation> findByProjectId(
            @Parameter(description = "Identifiant du projet.", example = "12")
            @PathVariable Long projectId
    ) {
        return projectInvitationService.findByProjectId(projectId);
    }

    /**
     * Retourne la vue publique d'une invitation a partir de son token.
     *
     * @param token token public de l'invitation
     * @return les metadonnees utiles au front
     */
    @GetMapping("/project-invitations/token/{token}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Résoudre une invitation par token",
            description = "Expose la vue publique minimale d'une invitation afin de permettre au frontend d'afficher l'écran d'acceptation sans authentification serveur."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation résolue."),
            @ApiResponse(responseCode = "404", description = "Token introuvable ou invitation inexistante.")
    })
    public ProjectInvitationPublicResponse findByToken(
            @Parameter(description = "Token public reçu par email.", example = "7f3b4b68-0f12-49e0-b17a-6a2d9d642111")
            @PathVariable String token
    ) {
        ProjectInvitation invitation = projectInvitationService.findByToken(token);
        return new ProjectInvitationPublicResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getProject().getId(),
                invitation.getProject().getName(),
                invitation.getExpiresAt()
        );
    }

    /**
     * Cree une nouvelle invitation.
     *
     * @param request invitation a enregistrer
     * @return identifiant genere
     */
    @PostMapping("/project-invitations")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
            summary = "Créer une invitation",
            description = "Crée une invitation, génère un token public, calcule l'expiration et déclenche l'envoi de l'email d'invitation."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Adresse email invitée, rôle accordé et contexte projet.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Invitation membre",
                    value = """
                            {
                              "email": "bob@pmt.fr",
                              "role": "MEMBER",
                              "projectId": 12,
                              "invitedById": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation créée et email déclenché."),
            @ApiResponse(responseCode = "400", description = "Invitation invalide ou déjà existante."),
            @ApiResponse(responseCode = "404", description = "Projet ou utilisateur introuvable.")
    })
    public Long create(@Valid @RequestBody ProjectInvitationCreateRequest request) {
        ProjectInvitation projectInvitation = new ProjectInvitation();
        projectInvitation.setEmail(request.email());
        projectInvitation.setRole(request.role());
        projectInvitation.setProject(projectService.findById(request.projectId()));
        projectInvitation.setInvitedBy(userService.findById(request.invitedById()));
        return projectInvitationService.create(projectInvitation);
    }

    /**
     * Accepte une invitation resolue par token.
     *
     * @param token token de l'invitation
     * @param request utilisateur courant cote front
     * @return le projet rejoint
     */
    @PostMapping("/project-invitations/token/{token}/accept")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Accepter une invitation",
            description = "Consomme une invitation à partir de son token et rattache l'utilisateur fourni au projet avec le rôle prévu par l'administrateur."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identifiant de l'utilisateur connecté côté frontend.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Acceptation standard",
                    value = """
                            {
                              "userId": 4
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation acceptée et projet rejoint."),
            @ApiResponse(responseCode = "400", description = "Invitation expirée, déjà consommée ou email non autorisé."),
            @ApiResponse(responseCode = "404", description = "Invitation ou utilisateur introuvable.")
    })
    public ProjectInvitationAcceptResponse accept(
            @Parameter(description = "Token public de l'invitation.", example = "7f3b4b68-0f12-49e0-b17a-6a2d9d642111")
            @PathVariable String token,
            @Valid @RequestBody ProjectInvitationAcceptRequest request
    ) {
        ProjectInvitation acceptedInvitation = projectInvitationService.accept(token, userService.findById(request.userId()));
        return new ProjectInvitationAcceptResponse(acceptedInvitation.getProject().getId());
    }

    /**
     * Annule une invitation en attente.
     *
     * @param id identifiant de l'invitation
     * @param request identifiant de l'admin demandeur
     */
    @PostMapping("/project-invitations/{id}/cancel")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Annuler une invitation",
            description = "Annule une invitation encore en attente sur demande d'un administrateur du projet."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identifiant de l'administrateur qui effectue l'action.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Annulation",
                    value = """
                            {
                              "requestedById": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation annulée."),
            @ApiResponse(responseCode = "400", description = "Action refusée."),
            @ApiResponse(responseCode = "404", description = "Invitation introuvable.")
    })
    public void cancel(
            @Parameter(description = "Identifiant de l'invitation à annuler.", example = "8")
            @PathVariable Long id,
            @Valid @RequestBody ProjectInvitationActionRequest request
    ) {
        projectInvitationService.cancel(id, request.requestedById());
    }

    /**
     * Renvoie une invitation existante.
     *
     * @param id identifiant de l'invitation
     * @param request identifiant de l'admin demandeur
     */
    @PostMapping("/project-invitations/{id}/resend")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Renvoyer une invitation",
            description = "Régénère un token public, repousse la date d'expiration et relance l'envoi de l'email d'invitation."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identifiant de l'administrateur à l'origine du renvoi.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Renvoi",
                    value = """
                            {
                              "requestedById": 1
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation renvoyée."),
            @ApiResponse(responseCode = "400", description = "Invitation non renvoyable."),
            @ApiResponse(responseCode = "404", description = "Invitation introuvable.")
    })
    public void resend(
            @Parameter(description = "Identifiant de l'invitation à renvoyer.", example = "8")
            @PathVariable Long id,
            @Valid @RequestBody ProjectInvitationActionRequest request
    ) {
        projectInvitationService.resend(id, request.requestedById());
    }

    /**
     * Supprime une invitation existante.
     *
     * @param id identifiant de l'invitation a supprimer
     */
    @DeleteMapping("/project-invitations/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(
            summary = "Supprimer une invitation",
            description = "Supprime définitivement une invitation par son identifiant interne."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitation supprimée."),
            @ApiResponse(responseCode = "404", description = "Invitation introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant interne de l'invitation à supprimer.", example = "8")
            @PathVariable Long id
    ) {
        ProjectInvitation projectInvitation = projectInvitationService.findById(id);
        projectInvitationService.delete(projectInvitation);
    }
}

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
    public ProjectInvitation findById(@PathVariable Long id) {
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
    public List<ProjectInvitation> findByProjectId(@PathVariable Long projectId) {
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
    public ProjectInvitationPublicResponse findByToken(@PathVariable String token) {
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
    public ProjectInvitationAcceptResponse accept(
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
    public void cancel(@PathVariable Long id, @Valid @RequestBody ProjectInvitationActionRequest request) {
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
    public void resend(@PathVariable Long id, @Valid @RequestBody ProjectInvitationActionRequest request) {
        projectInvitationService.resend(id, request.requestedById());
    }

    /**
     * Supprime une invitation existante.
     *
     * @param id identifiant de l'invitation a supprimer
     */
    @DeleteMapping("/project-invitations/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        ProjectInvitation projectInvitation = projectInvitationService.findById(id);
        projectInvitationService.delete(projectInvitation);
    }
}

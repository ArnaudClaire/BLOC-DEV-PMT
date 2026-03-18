package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectInvitationCreateRequest;
import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.services.ProjectInvitationService;
import com.mooc.formulaone.services.ProjectService;
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

    public ProjectInvitationController(
            ProjectInvitationService projectInvitationService,
            ProjectService projectService
    ) {
        this.projectInvitationService = projectInvitationService;
        this.projectService = projectService;
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
        projectInvitation.setStatus(request.status());
        projectInvitation.setProject(projectService.findById(request.projectId()));
        return projectInvitationService.create(projectInvitation);
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

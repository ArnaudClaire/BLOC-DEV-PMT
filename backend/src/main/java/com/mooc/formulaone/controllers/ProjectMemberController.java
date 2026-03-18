package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectMemberCreateRequest;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.ProjectMemberService;
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
import java.sql.Timestamp;

@RestController
/**
 * Expose les endpoints REST de gestion des membres de projet.
 */
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
    public ProjectMember findById(@PathVariable Long id) {
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
     * Supprime une association membre existante.
     *
     * @param id identifiant de l'association a supprimer
     */
    @DeleteMapping("/project-members/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        ProjectMember projectMember = projectMemberService.findById(id);
        projectMemberService.delete(projectMember);
    }
}

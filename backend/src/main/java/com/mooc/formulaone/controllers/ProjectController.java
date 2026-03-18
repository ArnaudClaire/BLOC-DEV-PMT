package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.ProjectCreateRequest;
import com.mooc.formulaone.bootstrap.TaskBoardColumnBootstrap;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import com.mooc.formulaone.services.ProjectMemberService;
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
 * Expose les endpoints REST de gestion des projets.
 */
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
    public Project findById(@PathVariable Long id) {
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
    public void delete(@PathVariable Long id) {
        Project project = projectService.findById(id);
        projectService.delete(project);
    }
}

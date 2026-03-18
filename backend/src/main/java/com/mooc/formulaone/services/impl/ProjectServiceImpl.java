package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.ProjectRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.services.ProjectService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implementation de {@link ProjectService} chargee d'encapsuler
 * l'acces au repository de projets.
 */
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Retourne tous les projets persistants.
     *
     * @return la liste des projets
     */
    @Override
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        projectRepository.findAll().forEach(projects::add);
        return projects;
    }

    /**
     * Retourne un projet par identifiant ou leve une exception metier 404.
     *
     * @param id identifiant du projet
     * @return le projet correspondant
     */
    @Override
    public Project findById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        if (project.isPresent()) {
            return project.get();
        }
        throw new EntityDontExistException();
    }

    /**
     * Persiste un nouveau projet.
     *
     * @param project projet a creer
     * @return identifiant genere
     */
    @Override
    public Long create(Project project) {
        return projectRepository.save(project).getId();
    }

    /**
     * Supprime le projet fourni.
     *
     * @param project projet a supprimer
     */
    @Override
    public void delete(Project project) {
        projectRepository.delete(project);
    }
}

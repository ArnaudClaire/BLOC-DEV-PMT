package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Project;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository Spring Data pour l'accès en base aux projets.
 */
public interface ProjectRepository extends CrudRepository<Project, Long> {
}

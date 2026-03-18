package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Project;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, Long> {
}

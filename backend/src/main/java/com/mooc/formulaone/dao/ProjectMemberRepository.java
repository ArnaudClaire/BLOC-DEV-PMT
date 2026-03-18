package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.ProjectMember;
import com.mooc.formulaone.models.ProjectRole;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProjectMemberRepository extends CrudRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    Optional<ProjectMember> findByProjectIdAndUserIdAndRole(Long projectId, Long userId, ProjectRole role);

    boolean existsByProjectIdAndUserEmailIgnoreCase(Long projectId, String email);
}

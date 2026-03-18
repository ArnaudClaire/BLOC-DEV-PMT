package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "project_members")
/**
 * Associe un utilisateur a un projet avec un role donne.
 */
public class ProjectMember extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    private Timestamp joinedAt;

    @ManyToOne
    @JsonIgnoreProperties({"members", "invitations", "tasks", "boardColumns"})
    private Project project;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User user;

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Met a jour le role du membre dans le projet.
     *
     * @param role nouveau role attribue
     */
    public void assignRole(ProjectRole role) {
        this.role = role;
    }
}

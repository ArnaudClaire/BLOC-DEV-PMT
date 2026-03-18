package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
/**
 * Represente un projet collaboratif gere dans l'application.
 */
public class Project extends BaseEntity {

    private String name;
    private String description;
    private LocalDate startDate;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User owner;

    @OneToMany(mappedBy = "project")
    @JsonIgnoreProperties("project")
    private Set<ProjectMember> members = new HashSet<>();

    @OneToMany(mappedBy = "project")
    @JsonIgnoreProperties("project")
    private Set<ProjectInvitation> invitations = new HashSet<>();

    @OneToMany(mappedBy = "project")
    @JsonIgnoreProperties("project")
    private Set<Task> tasks = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<ProjectMember> getMembers() {
        return members;
    }

    public void setMembers(Set<ProjectMember> members) {
        this.members = members;
    }

    public Set<ProjectInvitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(Set<ProjectInvitation> invitations) {
        this.invitations = invitations;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

}

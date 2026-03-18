package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
/**
 * Represente un utilisateur de la plateforme PMT.
 */
public class User extends BaseEntity {

    private String username;
    private String email;
    private String passwordHash;

    @OneToMany(mappedBy = "owner")
    @JsonIgnoreProperties("owner")
    private Set<Project> ownedProjects = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private Set<ProjectMember> projectMemberships = new HashSet<>();

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties("createdBy")
    private Set<Task> createdTasks = new HashSet<>();

    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnoreProperties("assignedTo")
    private Set<Task> assignedTasks = new HashSet<>();

    @OneToMany(mappedBy = "changedBy")
    @JsonIgnoreProperties("changedBy")
    private Set<TaskHistory> taskHistories = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties("user")
    private Set<Notification> notifications = new HashSet<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<Project> getOwnedProjects() {
        return ownedProjects;
    }

    public void setOwnedProjects(Set<Project> ownedProjects) {
        this.ownedProjects = ownedProjects;
    }

    public Set<ProjectMember> getProjectMemberships() {
        return projectMemberships;
    }

    public void setProjectMemberships(Set<ProjectMember> projectMemberships) {
        this.projectMemberships = projectMemberships;
    }

    public Set<Task> getCreatedTasks() {
        return createdTasks;
    }

    public void setCreatedTasks(Set<Task> createdTasks) {
        this.createdTasks = createdTasks;
    }

    public Set<Task> getAssignedTasks() {
        return assignedTasks;
    }

    public void setAssignedTasks(Set<Task> assignedTasks) {
        this.assignedTasks = assignedTasks;
    }

    public Set<TaskHistory> getTaskHistories() {
        return taskHistories;
    }

    public void setTaskHistories(Set<TaskHistory> taskHistories) {
        this.taskHistories = taskHistories;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

}

package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
/**
 * Represente une tache appartenant a un projet.
 */
public class Task extends BaseEntity {

    private String title;
    private String description;

    private String status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    private LocalDate dueDate;
    private LocalDate endDate;

    @ManyToOne
    @JsonIgnoreProperties({"tasks", "members", "invitations", "boardColumns"})
    private Project project;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User createdBy;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User assignedTo;

    @OneToMany(mappedBy = "task")
    @JsonIgnoreProperties("task")
    private Set<TaskHistory> histories = new HashSet<>();

    @OneToMany(mappedBy = "task")
    @JsonIgnoreProperties("task")
    private Set<Notification> notifications = new HashSet<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Set<TaskHistory> getHistories() {
        return histories;
    }

    public void setHistories(Set<TaskHistory> histories) {
        this.histories = histories;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Met a jour les principales informations fonctionnelles d'une tache.
     *
     * @param title titre fonctionnel
     * @param description description de travail
     * @param status statut courant
     * @param priority priorite associee
     * @param dueDate date d'echeance
     * @param endDate date de cloture
     */
    public void updateTask(
            String title,
            String description,
            String status,
            TaskPriority priority,
            LocalDate dueDate,
            LocalDate endDate
    ) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.endDate = endDate;
    }

    /**
     * Assigne la tache a un utilisateur.
     *
     * @param user utilisateur assigne
     */
    public void assignTo(User user) {
        this.assignedTo = user;
    }
}

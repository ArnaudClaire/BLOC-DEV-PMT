package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "notifications")
/**
 * Represente une notification adressee a un utilisateur.
 */
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private String message;
    private Timestamp sentAt;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"histories", "notifications"})
    private Task task;

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    /**
     * Marque la notification comme envoyee et renseigne la date d'envoi.
     */
    public void send() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Timestamp.from(Instant.now());
    }
}

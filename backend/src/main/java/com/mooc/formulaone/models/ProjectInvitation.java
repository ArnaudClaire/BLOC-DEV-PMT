package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "project_invitations")
/**
 * Represente une invitation envoyee a une adresse email pour rejoindre un projet.
 */
public class ProjectInvitation extends BaseEntity {

    private String email;

    @Column(unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private Timestamp expiresAt;

    private Timestamp acceptedAt;

    private Timestamp canceledAt;

    @ManyToOne
    @JsonIgnoreProperties({"members", "invitations", "tasks", "boardColumns"})
    private Project project;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User invitedBy;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User acceptedBy;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProjectRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Timestamp acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Timestamp getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(Timestamp canceledAt) {
        this.canceledAt = canceledAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }

    public User getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(User acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    /**
     * Marque l'invitation comme envoyee et en attente de reponse.
     */
    public void sendInvitation(String token, Timestamp expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.acceptedAt = null;
        this.canceledAt = null;
        this.status = InvitationStatus.PENDING;
    }

    /**
     * Accepte l'invitation courante.
     */
    public void accept(User acceptedBy, Timestamp acceptedAt) {
        this.acceptedBy = acceptedBy;
        this.acceptedAt = acceptedAt;
        this.status = InvitationStatus.ACCEPTED;
    }

    /**
     * Refuse l'invitation courante.
     */
    public void decline() {
        this.status = InvitationStatus.DECLINED;
    }

    public void cancel(Timestamp canceledAt) {
        this.canceledAt = canceledAt;
        this.status = InvitationStatus.CANCELED;
    }

    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }
}

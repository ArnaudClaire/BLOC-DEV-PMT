package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_invitations")
/**
 * Represente une invitation envoyee a une adresse email pour rejoindre un projet.
 */
public class ProjectInvitation extends BaseEntity {

    private String email;

    @Enumerated(EnumType.STRING)
    private ProjectRole role;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @ManyToOne
    @JsonIgnoreProperties({"members", "invitations", "tasks"})
    private Project project;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProjectRole getRole() {
        return role;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Marque l'invitation comme envoyee et en attente de reponse.
     */
    public void sendInvitation() {
        this.status = InvitationStatus.PENDING;
    }

    /**
     * Accepte l'invitation courante.
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    /**
     * Refuse l'invitation courante.
     */
    public void decline() {
        this.status = InvitationStatus.DECLINED;
    }
}

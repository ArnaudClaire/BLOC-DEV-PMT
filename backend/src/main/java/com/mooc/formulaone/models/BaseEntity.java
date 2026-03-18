package com.mooc.formulaone.models;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.sql.Timestamp;
import java.time.Instant;

@MappedSuperclass
/**
 * Base commune de toutes les entites persistantes du projet.
 * Centralise l'identifiant technique et les dates de creation/mise a jour.
 */
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    /**
     * Initialise automatiquement les dates d'audit lors de la creation.
     */
    @PrePersist
    public void onCreate() {
        Timestamp now = Timestamp.from(Instant.now());
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Met a jour automatiquement la date de modification avant sauvegarde.
     */
    @PreUpdate
    public void onUpdate() {
        updatedAt = Timestamp.from(Instant.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

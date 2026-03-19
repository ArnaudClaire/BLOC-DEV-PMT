package com.mooc.formulaone.models;

/**
 * Types d'événements historisés sur une tâche.
 */
public enum TaskHistoryAction {
    CREATED,
    UPDATED,
    ASSIGNED,
    STATUS_CHANGED,
    COMPLETED
}

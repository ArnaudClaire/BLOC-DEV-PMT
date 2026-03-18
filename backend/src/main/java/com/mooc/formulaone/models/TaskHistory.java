package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_histories")
/**
 * Trace une modification effectuee sur une tache.
 */
public class TaskHistory extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TaskHistoryAction actionType;

    private String fieldName;
    private String oldValue;
    private String newValue;

    @ManyToOne
    @JsonIgnoreProperties({"histories", "notifications"})
    private Task task;

    @ManyToOne
    @JsonIgnoreProperties({
            "ownedProjects", "projectMemberships", "createdTasks",
            "assignedTasks", "taskHistories", "notifications"
    })
    private User changedBy;

    public TaskHistoryAction getActionType() {
        return actionType;
    }

    public void setActionType(TaskHistoryAction actionType) {
        this.actionType = actionType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    /**
     * Enregistre les details d'un changement sur une tache.
     *
     * @param actionType type de changement
     * @param fieldName champ concerne
     * @param oldValue valeur precedente
     * @param newValue valeur suivante
     * @param task tache modifiee
     * @param changedBy utilisateur a l'origine du changement
     */
    public void recordChange(
            TaskHistoryAction actionType,
            String fieldName,
            String oldValue,
            String newValue,
            Task task,
            User changedBy
    ) {
        this.actionType = actionType;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.task = task;
        this.changedBy = changedBy;
    }
}

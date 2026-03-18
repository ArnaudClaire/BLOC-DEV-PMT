package com.mooc.formulaone.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_board_columns")
/**
 * Represente une colonne de board configurable pour un projet donne.
 */
public class TaskBoardColumn extends BaseEntity {

    private String name;
    private Integer displayOrder;

    @ManyToOne
    @JsonIgnoreProperties({"members", "invitations", "tasks", "boardColumns"})
    private Project project;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}

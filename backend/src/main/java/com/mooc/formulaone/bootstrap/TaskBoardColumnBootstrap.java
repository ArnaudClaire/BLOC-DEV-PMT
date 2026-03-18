package com.mooc.formulaone.bootstrap;

import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.TaskBoardColumn;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskBoardColumnService;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
/**
 * Initialise les colonnes de board par defaut et supprime les anciennes
 * contraintes de statut pour autoriser les etats personnalises.
 */
public class TaskBoardColumnBootstrap {

    private final JdbcTemplate jdbcTemplate;
    private final ProjectService projectService;
    private final TaskBoardColumnService taskBoardColumnService;

    public TaskBoardColumnBootstrap(
            JdbcTemplate jdbcTemplate,
            ProjectService projectService,
            TaskBoardColumnService taskBoardColumnService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.projectService = projectService;
        this.taskBoardColumnService = taskBoardColumnService;
    }

    @PostConstruct
    public void initialize() {
        dropLegacyTaskStatusChecksIfNeeded();
        ensureDefaultColumnsForExistingProjects();
    }

    private void ensureDefaultColumnsForExistingProjects() {
        projectService.findAll().forEach(project -> {
            if (taskBoardColumnService.findByProjectId(project.getId()).isEmpty()) {
                createDefaultColumns(project);
            }
        });
    }

    public void createDefaultColumns(Project project) {
        List<String> defaultColumns = List.of("TODO", "IN_PROGRESS", "DONE");
        for (int index = 0; index < defaultColumns.size(); index++) {
            TaskBoardColumn taskBoardColumn = new TaskBoardColumn();
            taskBoardColumn.setName(defaultColumns.get(index));
            taskBoardColumn.setDisplayOrder(index);
            taskBoardColumn.setProject(project);
            taskBoardColumnService.create(taskBoardColumn);
        }
    }

    private void dropLegacyTaskStatusChecksIfNeeded() {
        ConnectionCallback<String> databaseNameCallback =
                (connection) -> connection.getMetaData().getDatabaseProductName();
        String databaseProductName = jdbcTemplate.execute(databaseNameCallback);

        if (databaseProductName == null || !databaseProductName.toLowerCase().contains("postgres")) {
            return;
        }

        jdbcTemplate.execute("""
                DO $$
                DECLARE constraint_name text;
                BEGIN
                  FOR constraint_name IN
                    SELECT c.conname
                    FROM pg_constraint c
                    JOIN pg_class t ON c.conrelid = t.oid
                    WHERE t.relname = 'tasks'
                      AND c.contype = 'c'
                      AND pg_get_constraintdef(c.oid) ILIKE '%status%'
                  LOOP
                    EXECUTE format('ALTER TABLE public.tasks DROP CONSTRAINT IF EXISTS %I', constraint_name);
                  END LOOP;
                END $$;
                """);
    }
}

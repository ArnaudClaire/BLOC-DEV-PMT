package com.mooc.formulaone;

import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.Task;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.NotificationService;
import com.mooc.formulaone.services.ProjectInvitationService;
import com.mooc.formulaone.services.ProjectMemberService;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskHistoryService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
/**
 * Verifie que les donnees prechargees pour l'environnement de test
 * sont disponibles des l'initialisation du contexte Spring.
 */
class SeedDataIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private ProjectInvitationService projectInvitationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Autowired
    private NotificationService notificationService;

    @Test
    void shouldLoadSeedDataForTestEnvironment() {
        assertThat(userService.findAll())
                .extracting(User::getEmail)
                .contains("seed.owner@test.local", "seed.member@test.local", "seed.viewer@test.local");

        assertThat(projectService.findAll())
                .extracting(Project::getName)
                .contains("Seed Project");

        assertThat(projectMemberService.findAll()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(projectInvitationService.findAll()).hasSizeGreaterThanOrEqualTo(1);

        assertThat(taskService.findAll())
                .extracting(Task::getTitle)
                .contains("Seed Task");

        assertThat(taskHistoryService.findAll()).hasSizeGreaterThanOrEqualTo(1);

        assertThat(notificationService.findAll())
                .extracting(Notification::getMessage)
                .contains("Seed task assigned to the member.");
    }
}

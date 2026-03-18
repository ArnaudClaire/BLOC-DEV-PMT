package com.mooc.formulaone;

import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
/**
 * Verifie les interactions du service projet avec la persistence
 * et la relation obligatoire vers le proprietaire.
 */
class ProjectServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    /**
     * Verifie qu'un projet peut etre cree avec son proprietaire puis recupere intact.
     */
    @Test
    void shouldCreateProjectWithOwnerAndLoadItBack() {
        User owner = new User();
        owner.setUsername("owner");
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("secret");
        Long ownerId = userService.create(owner);

        Project project = new Project();
        project.setName("PMT");
        project.setDescription("Project management tool");
        project.setOwner(userService.findById(ownerId));
        Long projectId = projectService.create(project);

        Project persistedProject = projectService.findById(projectId);

        assertThat(projectId).isNotNull();
        assertThat(persistedProject.getName()).isEqualTo("PMT");
        assertThat(persistedProject.getOwner()).isNotNull();
        assertThat(persistedProject.getOwner().getEmail()).isEqualTo("owner@example.com");
    }
}

package com.mooc.formulaone;

import com.mooc.formulaone.models.Project;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.ProjectService;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test-user")
/**
 * Verifie les endpoints REST de consultation des taches sur des donnees
 * preparees via les services afin d'isoler le contrat de lecture HTTP.
 */
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    /**
     * Verifie qu'une tache creee via l'API peut ensuite etre recuperee
     * avec ses informations principales et ses relations utiles.
     */
    @Test
    void shouldCreateTaskAndFetchItThroughApi() throws Exception {
        User creator = new User();
        creator.setUsername("bob");
        creator.setEmail("bob@example.com");
        creator.setPasswordHash("hash");
        Long creatorId = userService.create(creator);

        Project project = new Project();
        project.setName("Board");
        project.setDescription("Task board");
        project.setOwner(userService.findById(creatorId));
        Long projectId = projectService.create(project);

        com.mooc.formulaone.models.Task task = new com.mooc.formulaone.models.Task();
        task.setTitle("Create API");
        task.setDescription("Implement PMT backend");
        task.setStatus("TODO");
        task.setPriority(com.mooc.formulaone.models.TaskPriority.HIGH);
        task.setProject(projectService.findById(projectId));
        task.setCreatedBy(userService.findById(creatorId));

        Long taskId = taskService.create(task);

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Create API"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.project.name").value("Board"))
                .andExpect(jsonPath("$.createdBy.email").value("bob@example.com"));
    }
}

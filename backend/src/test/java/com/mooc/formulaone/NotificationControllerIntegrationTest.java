package com.mooc.formulaone;

import static org.hamcrest.Matchers.hasItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.mooc.formulaone.models.NotificationStatus;
import com.mooc.formulaone.models.NotificationType;
import com.mooc.formulaone.models.User;
import com.mooc.formulaone.services.UserService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test-user")
/**
 * Verifie les endpoints REST de creation et de consultation des notifications.
 */
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    /**
     * Verifie qu'une notification creee via l'API apparait bien dans la liste retournee.
     */
    @Test
    void shouldCreateNotificationAndListIt() throws Exception {
        User user = new User();
        user.setUsername("eve");
        user.setEmail("eve@example.com");
        user.setPasswordHash("hash");
        Long userId = userService.create(user);

        String notificationJson = """
                {
                  "type": "%s",
                  "status": "%s",
                  "message": "A new task has been assigned to you",
                  "sentAt": "2026-03-14T12:00:00Z",
                  "userId": %d
                }
                """.formatted(NotificationType.TASK_ASSIGNED, NotificationStatus.SENT, userId);

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notificationJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", hasItem("TASK_ASSIGNED")))
                .andExpect(jsonPath("$[*].status", hasItem("SENT")))
                .andExpect(jsonPath("$[*].user.email", hasItem("eve@example.com")));
    }
}

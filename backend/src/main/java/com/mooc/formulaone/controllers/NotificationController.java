package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.NotificationCreateRequest;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.NotificationService;
import com.mooc.formulaone.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
/**
 * Expose les endpoints REST de gestion des notifications.
 */
@Tag(
        name = "Notifications",
        description = "Gestion des notifications applicatives, notamment celles liées aux tâches assignées."
)
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final TaskService taskService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService,
            TaskService taskService
    ) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.taskService = taskService;
    }

    /**
     * Retourne toutes les notifications.
     *
     * @return la liste des notifications
     */
    @GetMapping("/notifications")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Lister les notifications", description = "Retourne toutes les notifications connues par l'application.")
    @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès.")
    public List<Notification> findAll() {
        return notificationService.findAll();
    }

    /**
     * Retourne une notification a partir de son identifiant.
     *
     * @param id identifiant de la notification
     * @return la notification correspondante
     */
    @GetMapping("/notifications/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Récupérer une notification", description = "Retourne une notification à partir de son identifiant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification trouvée."),
            @ApiResponse(responseCode = "404", description = "Notification introuvable.")
    })
    public Notification findById(
            @Parameter(description = "Identifiant de la notification.", example = "9")
            @PathVariable Long id
    ) {
        return notificationService.findById(id);
    }

    /**
     * Cree une notification.
     *
     * @param request notification a enregistrer
     * @return identifiant genere
     */
    @PostMapping("/notifications")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(
            summary = "Créer une notification",
            description = "Crée une notification fonctionnelle rattachée à un utilisateur et, facultativement, à une tâche."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contenu de la notification à enregistrer.",
            required = true,
            content = @Content(examples = @ExampleObject(
                    name = "Notification d'assignation",
                    value = """
                            {
                              "type": "TASK_ASSIGNED",
                              "status": "SENT",
                              "message": "La tâche \\\"Préparer la démonstration\\\" vous a été assignée.",
                              "sentAt": "2026-03-19T10:15:00Z",
                              "userId": 4,
                              "taskId": 42
                            }
                            """
            ))
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification créée."),
            @ApiResponse(responseCode = "400", description = "Données invalides."),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou tâche introuvable.")
    })
    public Long create(@Valid @RequestBody NotificationCreateRequest request) {
        Notification notification = new Notification();
        notification.setType(request.type());
        notification.setStatus(request.status());
        notification.setMessage(request.message());
        if (request.sentAt() != null) {
            notification.setSentAt(java.sql.Timestamp.from(request.sentAt()));
        }
        notification.setUser(userService.findById(request.userId()));
        if (request.taskId() != null) {
            notification.setTask(taskService.findById(request.taskId()));
        }
        return notificationService.create(notification);
    }

    /**
     * Supprime une notification existante.
     *
     * @param id identifiant de la notification a supprimer
     */
    @DeleteMapping("/notifications/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Supprimer une notification", description = "Supprime une notification existante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification supprimée."),
            @ApiResponse(responseCode = "404", description = "Notification introuvable.")
    })
    public void delete(
            @Parameter(description = "Identifiant de la notification à supprimer.", example = "9")
            @PathVariable Long id
    ) {
        Notification notification = notificationService.findById(id);
        notificationService.delete(notification);
    }
}

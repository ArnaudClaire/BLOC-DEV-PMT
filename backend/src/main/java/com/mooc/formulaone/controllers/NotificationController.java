package com.mooc.formulaone.controllers;

import com.mooc.formulaone.controllers.dto.NotificationCreateRequest;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.services.TaskService;
import com.mooc.formulaone.services.NotificationService;
import com.mooc.formulaone.services.UserService;
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
    public Notification findById(@PathVariable Long id) {
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
    public void delete(@PathVariable Long id) {
        Notification notification = notificationService.findById(id);
        notificationService.delete(notification);
    }
}

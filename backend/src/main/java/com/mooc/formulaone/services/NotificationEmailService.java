package com.mooc.formulaone.services;

import com.mooc.formulaone.models.Notification;

/**
 * Gere l'envoi d'email pour les notifications applicatives qui le necessitent.
 */
public interface NotificationEmailService {

    /**
     * Envoie un email de notification lorsqu'une tache est assignee.
     *
     * @param notification notification a traduire en email
     */
    void sendTaskAssignmentNotification(Notification notification);
}

package com.mooc.formulaone.services;

import com.mooc.formulaone.models.Notification;

import java.util.List;

public interface NotificationService {

    /**
     * Retourne toutes les notifications.
     *
     * @return la liste des notifications
     */
    List<Notification> findAll();

    /**
     * Recherche une notification par identifiant.
     *
     * @param id identifiant de la notification
     * @return la notification correspondante
     */
    Notification findById(Long id);

    /**
     * Cree une notification.
     *
     * @param notification notification a enregistrer
     * @return identifiant genere
     */
    Long create(Notification notification);

    /**
     * Supprime une notification existante.
     *
     * @param notification notification a supprimer
     */
    void delete(Notification notification);
}

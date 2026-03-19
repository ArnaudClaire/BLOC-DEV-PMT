package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Notification;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository Spring Data pour les notifications utilisateur.
 */
public interface NotificationRepository extends CrudRepository<Notification, Long> {
}

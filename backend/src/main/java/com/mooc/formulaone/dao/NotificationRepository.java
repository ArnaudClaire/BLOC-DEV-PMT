package com.mooc.formulaone.dao;

import com.mooc.formulaone.models.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}

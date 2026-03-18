package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.dao.NotificationRepository;
import com.mooc.formulaone.exceptions.EntityDontExistException;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.services.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
/**
 * Implementation de {@link NotificationService} centralisant
 * les operations CRUD sur les notifications.
 */
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Retourne toutes les notifications stockees.
     *
     * @return la liste des notifications
     */
    @Override
    public List<Notification> findAll() {
        List<Notification> notifications = new ArrayList<>();
        notificationRepository.findAll().forEach(notifications::add);
        return notifications;
    }

    /**
     * Retourne une notification par identifiant ou leve une exception 404 metier
     * si aucune notification n'est trouvee.
     *
     * @param id identifiant de la notification
     * @return la notification correspondante
     */
    @Override
    public Notification findById(Long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        if (notification.isPresent()) {
            return notification.get();
        }
        throw new EntityDontExistException();
    }

    /**
     * Persiste une nouvelle notification.
     *
     * @param notification notification a enregistrer
     * @return identifiant genere
     */
    @Override
    public Long create(Notification notification) {
        return notificationRepository.save(notification).getId();
    }

    /**
     * Supprime la notification fournie.
     *
     * @param notification notification a supprimer
     */
    @Override
    public void delete(Notification notification) {
        notificationRepository.delete(notification);
    }
}

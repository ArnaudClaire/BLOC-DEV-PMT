package com.mooc.formulaone;

import com.mooc.formulaone.dao.NotificationRepository;
import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.models.NotificationType;
import com.mooc.formulaone.services.NotificationEmailService;
import com.mooc.formulaone.services.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationEmailService notificationEmailService;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NotificationServiceImpl(notificationRepository, notificationEmailService);
    }

    @Test
    void shouldSendEmailForTaskAssignmentNotifications() {
        Notification notification = new Notification();
        notification.setType(NotificationType.TASK_ASSIGNED);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        service.create(notification);

        verify(notificationEmailService).sendTaskAssignmentNotification(notification);
    }

    @Test
    void shouldNotSendEmailForNonAssignmentNotifications() {
        Notification notification = new Notification();
        notification.setType(NotificationType.INVITATION_SENT);

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        service.create(notification);

        verify(notificationEmailService, never()).sendTaskAssignmentNotification(any(Notification.class));
    }
}

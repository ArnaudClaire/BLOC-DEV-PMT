package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.models.Notification;
import com.mooc.formulaone.models.NotificationType;
import com.mooc.formulaone.services.NotificationEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
/**
 * Envoie les emails de notification lies aux assignations de taches ou journalise un fallback.
 */
public class NotificationEmailServiceImpl implements NotificationEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailFrom;

    public NotificationEmailServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:no-reply@pmt.local}") String mailFrom
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailFrom = mailFrom;
    }

    @Override
    public void sendTaskAssignmentNotification(Notification notification) {
        if (notification.getType() != NotificationType.TASK_ASSIGNED || notification.getUser() == null) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        String recipient = notification.getUser().getEmail();
        String taskTitle = notification.getTask() != null ? notification.getTask().getTitle() : "Tache PMT";
        String messageBody = notification.getMessage() != null ? notification.getMessage() : "Une tache vous a ete assignee.";

        if (mailSender == null) {
            LOGGER.info("Task assignment email fallback for {} about '{}': {}", recipient, taskTitle, messageBody);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject("Nouvelle tache assignee : " + taskTitle);
        message.setText("""
                Bonjour,

                %s

                Tache concernee : %s
                """.formatted(messageBody, taskTitle));
        mailSender.send(message);
    }
}

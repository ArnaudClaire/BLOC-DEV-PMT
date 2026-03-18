package com.mooc.formulaone.services.impl;

import com.mooc.formulaone.models.ProjectInvitation;
import com.mooc.formulaone.services.ProjectInvitationEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ProjectInvitationEmailServiceImpl implements ProjectInvitationEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInvitationEmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String frontendBaseUrl;
    private final String mailFrom;

    public ProjectInvitationEmailServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.frontend-base-url:http://localhost:4200}") String frontendBaseUrl,
            @Value("${app.mail.from:no-reply@pmt.local}") String mailFrom
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.frontendBaseUrl = frontendBaseUrl;
        this.mailFrom = mailFrom;
    }

    @Override
    public void sendInvitation(ProjectInvitation invitation) {
        String invitationLink = "%s/invitation/%s".formatted(frontendBaseUrl, invitation.getToken());
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            LOGGER.info(
                    "Invitation email fallback for {} to join project '{}' as {}. Link: {}",
                    invitation.getEmail(),
                    invitation.getProject().getName(),
                    invitation.getRole(),
                    invitationLink
            );
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(invitation.getEmail());
        message.setSubject("Invitation au projet " + invitation.getProject().getName());
        message.setText("""
                Bonjour,

                Vous avez ete invite a rejoindre le projet "%s" avec le role %s.

                Utilisez ce lien pour vous connecter ou creer votre compte, puis accepter l'invitation :
                %s

                Ce lien expire le %s.
                """.formatted(
                invitation.getProject().getName(),
                invitation.getRole(),
                invitationLink,
                invitation.getExpiresAt()
        ));

        mailSender.send(message);
    }
}

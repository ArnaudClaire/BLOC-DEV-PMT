package com.mooc.formulaone.services;

import com.mooc.formulaone.models.ProjectInvitation;

/**
 * Gere l'envoi de l'email d'invitation vers le destinataire.
 */
public interface ProjectInvitationEmailService {

    /**
     * Envoie le message d'invitation avec le lien public d'acceptation.
     *
     * @param invitation invitation a transmettre
     */
    void sendInvitation(ProjectInvitation invitation);
}

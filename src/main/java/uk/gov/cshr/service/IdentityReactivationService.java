package uk.gov.cshr.service;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;

@Service
public class IdentityReactivationService {

    private NotificationService notificationService;

    private MessageService messageService;

    private ReactivationService reactivationService;

    public IdentityReactivationService(NotificationService notificationService,
                                       MessageService messageService,
                                       ReactivationService reactivationService) {
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.reactivationService = reactivationService;
    }

    public boolean sendReactivationEmail(Identity identity) {
        Reactivation reactivation = reactivationService.createReactivationRequest(identity.getEmail());

        return notificationService.send(messageService.createReactivationMessage(identity, reactivation));
    }
}

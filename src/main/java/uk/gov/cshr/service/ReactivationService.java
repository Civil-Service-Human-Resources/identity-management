package uk.gov.cshr.service;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.ReactivationRepository;

import java.util.Comparator;
import java.util.Date;

@Service
public class ReactivationService {

    private ReactivationRepository reactivationRepository;
    private MessageService messageService;
    private NotificationService notificationService;

    public ReactivationService(ReactivationRepository reactivationRepository,
                               MessageService messageService,
                               NotificationService notificationService) {
        this.reactivationRepository = reactivationRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
    }

    public Reactivation createReactivationRequest(String email) {
        String code = RandomStringUtils.random(40, true, true);

        Reactivation reactivation = new Reactivation(code, ReactivationStatus.PENDING, new Date(), email);

        return reactivationRepository.save(reactivation);
    }

    public void sendReactivationEmail(Identity identity, Reactivation reactivation) {
        notificationService.send(messageService.createReactivationMessage(identity, reactivation));
    }

    public Date getLatestReactivationForEmail(String email) {
        Date latestReactivationDate = null;
        Reactivation latestReactivation = reactivationRepository.findByEmailAndReactivationStatusEquals(email, ReactivationStatus.REACTIVATED)
                .stream()
                .max(Comparator.comparing(Reactivation::getReactivatedAt)).orElse(null);
        if (latestReactivation != null) {
            latestReactivationDate = latestReactivation.getReactivatedAt();
        }
        return latestReactivationDate;
    }
}

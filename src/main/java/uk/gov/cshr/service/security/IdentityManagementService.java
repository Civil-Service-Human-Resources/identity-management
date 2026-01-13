package uk.gov.cshr.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.csrs.CsrsService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;
import uk.gov.cshr.service.reportingService.ReportingService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static uk.gov.cshr.utils.Util.batchList;

@Service
@Slf4j
@Transactional
public class IdentityManagementService {

    private final IdentityRepository identityRepository;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final LearnerRecordService learnerRecordService;
    private final CsrsService csrsService;
    private final ReportingService reportingService;
    private final InviteService inviteService;
    private final ResetService resetService;
    private final Integer deactivationBatchSize;

    public IdentityManagementService(IdentityRepository identityRepository, MessageService messageService,
                                     NotificationService notificationService, LearnerRecordService learnerRecordService,
                                     CsrsService csrsService, ReportingService reportingService, InviteService inviteService,
                                     ResetService resetService,
                                     @Value("${dataRetentionJob.deactivationBatchSize}") Integer deactivationBatchSize) {
        this.identityRepository = identityRepository;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.learnerRecordService = learnerRecordService;
        this.csrsService = csrsService;
        this.reportingService = reportingService;
        this.inviteService = inviteService;
        this.resetService = resetService;
        this.deactivationBatchSize = deactivationBatchSize;
    }

    @Transactional(propagation = REQUIRED, isolation = SERIALIZABLE, rollbackFor = Exception.class)
    public void deleteIdentity(Identity identity) {
        log.info("Deleting identity: {}", identity.getEmail());
        String uid = identity.getUid();
        log.info("Deleting from learner-record");
        learnerRecordService.deleteCivilServant(uid);
        log.info("Deleting from civil-servant-registry");
        csrsService.deleteCivilServant(uid);
        reportingService.removeUserDetails(uid);
        inviteService.deleteInvitesByIdentity(identity);
        resetService.deleteResetsByIdentity(identity);
        identityRepository.delete(identity);
        identityRepository.flush();
        notificationService.send(messageService.createDeletedMessage(identity));
    }

    public void markUsersForDeletion(List<Identity> identities) {
        log.info("Marking identities for deletion {}", identities.stream().map(Identity::getUid).collect(Collectors.joining(", ")));
        identities.forEach(i -> i.setDeletionNotificationSent(true));
        identityRepository.save(identities);
        identityRepository.flush();
        notificationService.send(identities.stream().map(messageService::createDeletionMessage).collect(Collectors.toList()));
    }

    @Transactional(propagation = REQUIRED, isolation = SERIALIZABLE, rollbackFor = Exception.class)
    public void deactivateIdentities(List<Identity> identities) {
        log.info("Deactivating {} identities", identities.size());
        batchList(identities, deactivationBatchSize)
                .forEach(identityBatch -> {
                    identityBatch.forEach(u -> u.setActive(false));
                    reportingService.deactivateRegisteredLearners(identityBatch.stream().map(Identity::getUid).collect(Collectors.toList()));
                    identityRepository.save(identityBatch);
                    identityRepository.flush();
                    notificationService.send(identityBatch.stream().map(messageService::createSuspensionMessage).collect(Collectors.toList()));
                });
    }

    public void deactivateIdentity(Identity identity) {
        deactivateIdentities(Collections.singletonList(identity));
    }
}

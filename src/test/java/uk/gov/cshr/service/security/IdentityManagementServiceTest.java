package uk.gov.cshr.service.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.identity.entity.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.identity.repository.IdentityRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.csrs.CsrsService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;
import uk.gov.cshr.service.reportingService.ReportingService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdentityManagementServiceTest {

    private final IdentityRepository identityRepository = mock(IdentityRepository.class);
    private final MessageService messageService = mock(MessageService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final LearnerRecordService learnerRecordService = mock(LearnerRecordService.class);
    private final CsrsService csrsService = mock(CsrsService.class);
    private final ReportingService reportingService = mock(ReportingService.class);
    private final InviteService inviteService = mock(InviteService.class);
    private final ResetService resetService = mock(ResetService.class);

    @InjectMocks
    private IdentityManagementService identityManagementService = new IdentityManagementService(
            identityRepository,
            messageService,
            notificationService,
            learnerRecordService,
            csrsService,
            reportingService,
            inviteService,
            resetService,
            50
    );


    @Test
    public void testDeleteIdentity() {
        Identity identity = new Identity();
        identity.setUid("uid");

        MessageDto messageDto = new MessageDto();
        when(messageService.createDeletedMessage(identity)).thenReturn(messageDto);

        identityManagementService.deleteIdentity(identity);
        verify(reportingService, times(1)).removeUserDetails("uid");
        verify(learnerRecordService, times(1)).deleteCivilServant("uid");
        verify(csrsService, times(1)).deleteCivilServant("uid");
        verify(inviteService, times(1)).deleteInvitesByIdentity(identity);
        verify(resetService, times(1)).deleteResetsByIdentity(identity);
        verify(identityRepository, times(1)).delete(identity);
        verify(messageService, times(1)).createDeletedMessage(identity);
        verify(notificationService, times(1)).send(messageDto);
    }

    @Test
    public void testMarkForDeletion() {
        Identity identity = new Identity();
        identity.setUid("uid");
        identity.setDeletionNotificationSent(false);

        List<Identity> list = Collections.singletonList(identity);

        MessageDto messageDto = new MessageDto();
        when(messageService.createDeletionMessage(identity)).thenReturn(messageDto);

        identityManagementService.markUsersForDeletion(list);
        verify(identityRepository, times(1)).saveAll(list);
        verify(messageService, times(1)).createDeletionMessage(identity);
        verify(notificationService, times(1)).send(Collections.singletonList(messageDto));

        assertTrue(identity.isDeletionNotificationSent());
    }

    @Test
    public void testDeactivateIdentities() {
        Identity identity = new Identity();
        identity.setUid("uid");
        identity.setActive(true);

        List<Identity> list = Collections.singletonList(identity);

        MessageDto messageDto = new MessageDto();
        when(messageService.createSuspensionMessage(identity)).thenReturn(messageDto);

        identityManagementService.deactivateIdentities(list);
        verify(reportingService, times(1)).deactivateRegisteredLearners(Collections.singletonList("uid"));
        verify(identityRepository, times(1)).saveAll(list);
        verify(messageService, times(1)).createSuspensionMessage(identity);
        verify(notificationService, times(1)).send(List.of(messageDto));

        assertFalse(identity.isActive());
    }


}

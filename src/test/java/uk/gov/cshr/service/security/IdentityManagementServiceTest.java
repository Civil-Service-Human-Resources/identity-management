package uk.gov.cshr.service.security;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.csrs.CSRSService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;
import uk.gov.cshr.service.reportingService.ReportingService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityManagementServiceTest extends TestCase {

    @Mock
    private IdentityRepository identityRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private LearnerRecordService learnerRecordService;
    @Mock
    private CSRSService csrsService;
    @Mock
    private ReportingService reportingService;
    @Mock
    private InviteService inviteService;
    @Mock
    private ResetService resetService;

    @InjectMocks
    private IdentityManagementService identityManagementService;


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
        verify(identityRepository, times(1)).save(list);
        verify(messageService, times(1)).createDeletionMessage(identity);
        verify(notificationService, times(1)).send(Collections.singletonList(messageDto));

        assertTrue(identity.isDeletionNotificationSent());
    }

    public void testDeactivateIdentities() {
        Identity identity = new Identity();
        identity.setUid("uid");
        identity.setActive(true);

        List<Identity> list = Collections.singletonList(identity);

        MessageDto messageDto = new MessageDto();
        when(messageService.createSuspensionMessage(identity)).thenReturn(messageDto);

        identityManagementService.deactivateIdentities(list);
        verify(reportingService, times(1)).deactivateRegisteredLearners(Collections.singletonList("uid"));
        verify(identityRepository, times(1)).save(list);
        verify(messageService, times(1)).createSuspensionMessage(identity);
        verify(notificationService, times(1)).send(messageDto);

        assertFalse(identity.isActive());
    }


}

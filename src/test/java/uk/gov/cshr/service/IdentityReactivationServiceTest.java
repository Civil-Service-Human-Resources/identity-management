package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;

import java.time.Instant;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdentityReactivationServiceTest {

    private static final String EMAIL = "test@example.com";
    @Mock
    private NotificationService notificationService;

    @Mock
    private MessageService messageService;

    @Mock
    private ReactivationService reactivationService;

    @InjectMocks
    private IdentityReactivationService identityReactivationService;

    @Test
    public void shouldSendReactivationEmail() {
        Identity identity = new Identity("uid",
                EMAIL,
                "password",
                true,
                false,
                null,
                Instant.now(),
                false, null);
        Reactivation reactivation = new Reactivation();
        MessageDto messageDto = new MessageDto();

        when(reactivationService.createReactivationRequest(EMAIL)).thenReturn(reactivation);
        when(messageService.createReactivationMessage(identity, reactivation)).thenReturn(messageDto);
        when(notificationService.send(messageDto)).thenReturn(true);
        assertTrue(identityReactivationService.sendReactivationEmail(identity));
    }
}
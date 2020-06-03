package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private static final String UID = "UID";
    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private InviteService inviteService;

    @Mock
    private ResetService resetService;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LearnerRecordService learnerRecordService;

    @Mock
    private CSRSService csrsService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MessageService messageService;

    @Mock
    private IdentityReactivationService identityReactivationService;

    private int deactivationMonths = 10;

    private int notificationMonths = 20;

    private int deletionMonths = 36;

    private IdentityService identityService;

    @Before
    public void setUp() throws Exception {
        identityService = new IdentityService(deactivationMonths,
                notificationMonths,
                deletionMonths,
                identityRepository,
                passwordEncoder,
                learnerRecordService,
                csrsService,
                notificationService,
                messageService,
                resetService,
                tokenService,
                identityReactivationService);
    }

    @Test
    public void shouldSetActiveToFalseIfActive() {
        Identity identity = new Identity();
        identity.setActive(true);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        when(identityRepository.save(identity)).thenReturn(identity);

        identityService.updateActive(UID);

        verify(identityReactivationService, times(0)).sendReactivationEmail(any(Identity.class));
    }

    @Test
    public void shouldCallReactivationServiceIfActiveIsFalse() {
        Identity identity = new Identity();
        identity.setActive(false);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));


        identityService.updateActive(UID);

        verify(identityRepository, times(0)).save(any(Identity.class));

        verify(identityReactivationService, times(1)).sendReactivationEmail(identity);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundIfIdentityDoesNotExist() {
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        identityService.updateActive(UID);

        verify(identityRepository, times(0)).save(any(Identity.class));

        verify(identityReactivationService, times(0)).sendReactivationEmail(any(Identity.class));
    }
}

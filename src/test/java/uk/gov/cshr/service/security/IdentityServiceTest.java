package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private static final String UID = "UID";
    private static final Long ID = 1L;
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

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

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

        identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);
    }

    @Test
    public void shouldGetIdentity() {
        Identity identity = new Identity();
        identity.setId(ID);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        Identity actualIdentity = identityService.getIdentity(UID);

        assertEquals(ID, actualIdentity.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowExceptionIfIdentityNotFound() {
        doThrow(new ResourceNotFoundException()).when(identityRepository).findFirstByUid(UID);

        identityService.getIdentity(UID);
    }

    @Test
    public void shouldSetLockedToFalseIfLockedIsTrue() {
        Identity identity = new Identity();
        identity.setLocked(true);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        when(identityRepository.save(identity)).thenReturn(identity);

        identityService.updateLocked(UID);

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertEquals(false, actualIdentity.isLocked());
    }

    @Test
    public void shouldSetLockedToTrueIfLockedIsFalse() {
        Identity identity = new Identity();
        identity.setLocked(false);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        when(identityRepository.save(identity)).thenReturn(identity);

        identityService.updateLocked(UID);

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertEquals(true, actualIdentity.isLocked());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundIfIdentityDoesNotExistWhenUpdatedLocked() {
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        identityService.updateLocked(UID);

        verify(identityRepository, times(0)).save(any(Identity.class));
    }
}

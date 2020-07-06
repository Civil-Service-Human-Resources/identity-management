package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.TokenService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private static final String UID = "UID";
    private static final Long ID = 1L;
    private static final String AGENCY_TOKEN_UID = "UID";
    @Mock
    private IdentityRepository identityRepository;

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
                tokenService);

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

    @Test
    public void shouldDeactivateAgencyUser() {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setAgencyTokenUid(AGENCY_TOKEN_UID);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(deactivationMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        MessageDto messageDto = new MessageDto();
        when(identityRepository.findAll()).thenReturn(identities);
        when(messageService.createSuspensionMessage(identity)).thenReturn(messageDto);
        when(notificationService.send(messageDto)).thenReturn(true);

        identityService.trackUserActivity();

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertFalse(actualIdentity.isActive());
        assertNull(actualIdentity.getAgencyTokenUid());
    }

    @Test
    public void shouldDeactivateNonAgencyUser() {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setAgencyTokenUid(null);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(deactivationMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        MessageDto messageDto = new MessageDto();
        when(identityRepository.findAll()).thenReturn(identities);
        when(messageService.createSuspensionMessage(identity)).thenReturn(messageDto);
        when(notificationService.send(messageDto)).thenReturn(true);

        identityService.trackUserActivity();

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertFalse(actualIdentity.isActive());
        assertNull(actualIdentity.getAgencyTokenUid());
    }

    @Test
    public void shouldNotDeactivateIfAlreadyDeactivated() {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(deactivationMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        when(identityRepository.findAll()).thenReturn(identities);

        identityService.trackUserActivity();

        verify(notificationService, times(0)).send(any(MessageDto.class));
        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void shouldSendDeletionNotification() {
        Identity identity = new Identity();
        identity.setDeletionNotificationSent(false);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(notificationMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        MessageDto messageDto = new MessageDto();
        when(identityRepository.findAll()).thenReturn(identities);
        when(messageService.createDeletionMessage(identity)).thenReturn(messageDto);
        when(notificationService.send(messageDto)).thenReturn(true);

        identityService.trackUserActivity();

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertTrue(actualIdentity.isDeletionNotificationSent());
    }

    @Test
    public void shouldNotSendDeletionNotificationIfAlreadySent() {
        Identity identity = new Identity();
        identity.setDeletionNotificationSent(true);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(notificationMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        when(identityRepository.findAll()).thenReturn(identities);

        identityService.trackUserActivity();

        verify(notificationService, times(0)).send(any(MessageDto.class));
        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void shouldNotSendDeletionNotificationIfNotDue() {
        Identity identity = new Identity();
        identity.setDeletionNotificationSent(false);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(notificationMonths)
                .plusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        when(identityRepository.findAll()).thenReturn(identities);

        identityService.trackUserActivity();

        verify(notificationService, times(0)).send(any(MessageDto.class));
        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void shouldDeleteUser() {
        Identity identity = new Identity();
        identity.setUid(UID);
        identity.setLastLoggedIn(LocalDateTime.now()
                .minusMonths(deletionMonths)
                .minusDays(1)
                .toInstant(ZoneOffset.UTC));

        List<Identity> identities = new ArrayList<>();
        identities.add(identity);

        MessageDto messageDto = new MessageDto();

        when(identityRepository.findAll()).thenReturn(identities);
        when(messageService.createDeletedMessage(identity)).thenReturn(messageDto);
        when(notificationService.send(messageDto)).thenReturn(true);

        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK);
        when(learnerRecordService.deleteCivilServant(any(String.class))).thenReturn(responseEntity);

        identityService.trackUserActivity();
        verify(notificationService, times(1)).send(any(MessageDto.class));

    }
}

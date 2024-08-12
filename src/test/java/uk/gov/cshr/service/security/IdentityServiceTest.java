package uk.gov.cshr.service.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.csrs.CSRSService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;
import uk.gov.cshr.service.reportingService.ReportingService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdentityServiceTest {

    private static final String UID = "UID";
    private static final Long ID = 1L;

    private static final int DEACTIVATION_MONTH = 1;
    private static final int NOTIFICATION_MONTH = 2;
    private static final int DELETION_MONTH = 3;

    // LLIT - Last Logged In Time
    // Remove an extra day from the LLIT avoid any issue with tests running to quickly
    private static final Instant DEACTIVATION_LLIT = LocalDateTime.now().minusMonths(DEACTIVATION_MONTH).minusDays(1).toInstant(ZoneOffset.UTC);
    private static final Instant NOTIFICATION_LLIT = LocalDateTime.now().minusMonths(NOTIFICATION_MONTH).minusDays(1).toInstant(ZoneOffset.UTC);
    private static final Instant DELETION_LLIT = LocalDateTime.now().minusMonths(DELETION_MONTH).minusDays(1).toInstant(ZoneOffset.UTC);

    private static final boolean USER_ACTIVE = true;
    private static final boolean USER_DEACTIVATED = false;

    private static final boolean USER_NOT_LOCKED = false;

    private static final boolean DELETE_NOTIFICATION_SENT = true;
    private static final boolean DELETE_NOTIFICATION_NOT_SENT = false;

    private static final Set<Role> DEFAULT_ROLE_SET = Collections.EMPTY_SET;

    @Mock
    private IdentityRepository identityRepository;
    @Mock
    private InviteService inviteService;
    @Mock
    private ResetService resetService;
    @Mock
    private LearnerRecordService learnerRecordService;
    @Mock
    private CSRSService csrsService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MessageService messageService;
    @Mock
    private ReportingService reportingService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RequestEntityFactory requestEntityFactory;
    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    private IdentityService identityService;

    private RequestEntity requestEntity = RequestEntity.get(null).build();
    private ResponseEntity responseEntity = ResponseEntity.ok().build();

    @Before
    public void createIdentityService() {
        identityService = new IdentityService(identityRepository, learnerRecordService, csrsService, reportingService, resetService, restTemplate, requestEntityFactory);
        identityService.setInviteService(inviteService);

        when(requestEntityFactory.createLogoutRequest()).thenReturn(requestEntity);
        when(restTemplate.exchange(requestEntity, Void.class)).thenReturn(responseEntity);

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

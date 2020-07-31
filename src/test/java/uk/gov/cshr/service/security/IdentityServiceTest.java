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
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

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
    private TokenService tokenService;
    @Mock
    private LearnerRecordService learnerRecordService;
    @Mock
    private CSRSService csrsService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MessageService messageService;
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
        identityService = new IdentityService(DEACTIVATION_MONTH, NOTIFICATION_MONTH, DELETION_MONTH, identityRepository, learnerRecordService, csrsService, notificationService, messageService, resetService, tokenService, restTemplate, requestEntityFactory);
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

    @Test
    public void trackUserActivity_NoActionsWhenNoUsersInRanges() {

        Identity noActionUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, Instant.now(), DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        List<Identity> userList = new ArrayList<>();
        userList.add(noActionUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        assertTrue(noActionUser.isActive());
        assertFalse(noActionUser.isDeletionNotificationSent());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_DeactivationWhenUserInDeactivationRange() {

        Identity deactivationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DEACTIVATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());
        MessageDto deactivationNotification = new MessageDto();

        List<Identity> userList = new ArrayList<>();
        userList.add(deactivationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createSuspensionMessage(deactivationUser)).thenReturn(deactivationNotification);

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createSuspensionMessage(deactivationUser);
        verify(notificationService, times(1)).send(deactivationNotification);
        verify(identityRepository, times(1)).saveAndFlush(deactivationUser);

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        assertFalse(deactivationUser.isActive());
        assertNull(deactivationUser.getAgencyTokenUid());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_NoDeactivationWhenUserInDeactivationRangeButAlreadyDeactivated() {
        Identity deactivationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DEACTIVATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        List<Identity> userList = new ArrayList<>();
        userList.add(deactivationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }


    @Test
    public void trackUserActivity_NotificationWhenUserInNotificationRange() {
        Identity notificationUserDeactivated = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, null);
        Identity notificationUserActive = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        MessageDto deletionWarningNotification = new MessageDto();

        List<Identity> userList = new ArrayList<>();
        userList.add(notificationUserDeactivated);
        userList.add(notificationUserActive);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createDeletionMessage(notificationUserDeactivated)).thenReturn(deletionWarningNotification);
        when(messageService.createDeletionMessage(notificationUserActive)).thenReturn(deletionWarningNotification);

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createDeletionMessage(notificationUserActive);
        verify(messageService, times(1)).createDeletionMessage(notificationUserDeactivated);
        verify(notificationService, times(2)).send(deletionWarningNotification);
        verify(identityRepository, times(1)).saveAndFlush(notificationUserDeactivated);
        verify(identityRepository, times(1)).saveAndFlush(notificationUserActive);
        verifyNoMoreInteractions(identityRepository);

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        for (Identity user : userList) {
            assertTrue(user.isDeletionNotificationSent());
        }

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_NoNotificationSentWhenUserInNotificationRangeButNotificationAlreadySent() {
        Identity notificationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_SENT, null);

        List<Identity> userList = new ArrayList<>();
        userList.add(notificationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_DeletionWhenUserInDeletionRange() {
        Identity deletionUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DELETION_LLIT, DELETE_NOTIFICATION_SENT, null);
        MessageDto deletionNotification = new MessageDto();
        List<Identity> userList = new ArrayList<>();
        userList.add(deletionUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createDeletedMessage(deletionUser)).thenReturn(deletionNotification);
        when(learnerRecordService.deleteCivilServant(deletionUser.getUid())).thenReturn(ResponseEntity.noContent().build());
        when(csrsService.deleteCivilServant(deletionUser.getUid())).thenReturn(ResponseEntity.noContent().build());
        when(identityRepository.findFirstByUid(deletionUser.getUid())).thenReturn(Optional.of(deletionUser));

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createDeletedMessage(deletionUser);
        verify(notificationService, times(1)).send(deletionNotification);
        verify(learnerRecordService, times(1)).deleteCivilServant(deletionUser.getUid());
        verify(csrsService, times(1)).deleteCivilServant(deletionUser.getUid());
        verify(identityRepository, times(1)).findFirstByUid(deletionUser.getUid());
        verify(inviteService, times(1)).deleteInvitesByIdentity(deletionUser);
        verify(resetService, times(1)).deleteResetsByIdentity(deletionUser);
        verify(tokenService, times(1)).deleteTokensByIdentity(deletionUser);
        verify(identityRepository, times(1)).delete(deletionUser);
        verify(identityRepository, times(1)).flush();

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_MixActionWhenUsersInMixRange() {
        Identity deactivationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DEACTIVATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());
        Identity notificationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, null);
        Identity deletionUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DELETION_LLIT, DELETE_NOTIFICATION_SENT, null);

        MessageDto deactivationNotification = new MessageDto();
        MessageDto deletionWarningNotification = new MessageDto();
        MessageDto deletionNotification = new MessageDto();

        deactivationNotification.setTemplateId(UUID.randomUUID().toString());
        deletionWarningNotification.setTemplateId(UUID.randomUUID().toString());
        deletionNotification.setTemplateId(UUID.randomUUID().toString());

        List<Identity> userList = new ArrayList<>();
        userList.add(deactivationUser);
        userList.add(notificationUser);
        userList.add(deletionUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createSuspensionMessage(deactivationUser)).thenReturn(deactivationNotification);
        when(messageService.createDeletionMessage(notificationUser)).thenReturn(deletionWarningNotification);
        when(messageService.createDeletedMessage(deletionUser)).thenReturn(deletionNotification);
        when(learnerRecordService.deleteCivilServant(deletionUser.getUid())).thenReturn(ResponseEntity.noContent().build());
        when(csrsService.deleteCivilServant(deletionUser.getUid())).thenReturn(ResponseEntity.noContent().build());
        when(identityRepository.findFirstByUid(deletionUser.getUid())).thenReturn(Optional.of(deletionUser));

        identityService.trackUserActivity();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());

        verify(notificationService, times(1)).send(deactivationNotification);
        verify(notificationService, times(1)).send(deletionWarningNotification);
        verify(notificationService, times(1)).send(deletionNotification);

        verify(identityRepository, times(1)).saveAndFlush(deactivationUser);
        verify(identityRepository, times(1)).saveAndFlush(notificationUser);
        verify(identityRepository, times(1)).delete(deletionUser);

        verify(messageService, times(1)).createSuspensionMessage(deactivationUser);
        verify(messageService, times(1)).createDeletionMessage(notificationUser);
        verify(messageService, times(1)).createDeletedMessage(deletionUser);

        verify(learnerRecordService, times(1)).deleteCivilServant(deletionUser.getUid());
        verify(csrsService, times(1)).deleteCivilServant(deletionUser.getUid());
        verify(identityRepository, times(1)).findFirstByUid(deletionUser.getUid());
        verify(inviteService, times(1)).deleteInvitesByIdentity(deletionUser);
        verify(resetService, times(1)).deleteResetsByIdentity(deletionUser);
        verify(tokenService, times(1)).deleteTokensByIdentity(deletionUser);
        verify(identityRepository, times(1)).flush();

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, learnerRecordService, csrsService, inviteService, resetService, tokenService);

        assertFalse(deactivationUser.isActive());
        assertNull(deactivationUser.getAgencyTokenUid());
        assertTrue(notificationUser.isDeletionNotificationSent());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void trackUserActivity_NoErrorWhenNoUsers() {
        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(Collections.EMPTY_LIST);
        identityService.trackUserActivity();

        verifyZeroInteractions(notificationService, learnerRecordService, csrsService, inviteService, resetService, tokenService);
        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verifyNoMoreInteractions(identityRepository);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }
}
package uk.gov.cshr.service;

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
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.dataRetentionJob.DataRetentionJobService;
import uk.gov.cshr.service.security.IdentityService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DataRetentionJobServiceTest {

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
    private IdentityService identityService;

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

    private RequestEntity requestEntity = RequestEntity.get(null).build();
    private ResponseEntity responseEntity = ResponseEntity.ok().build();

    private DataRetentionJobService dataRetentionJobService;

    @Before
    public void createDataRetentionJobService() {
        dataRetentionJobService = new DataRetentionJobService(restTemplate, requestEntityFactory);

        when(requestEntityFactory.createLogoutRequest()).thenReturn(requestEntity);
        when(restTemplate.exchange(requestEntity, Void.class)).thenReturn(responseEntity);

        identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);
    }

    @Test
    public void dataRetentionJob_NoActionsWhenNoUsersInRanges() {

        Identity noActionUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, Instant.now(), DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        List<Identity> userList = new ArrayList<>();
        userList.add(noActionUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        assertTrue(noActionUser.isActive());
        assertFalse(noActionUser.isDeletionNotificationSent());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_DeactivationWhenUserInDeactivationRange() {

        Identity deactivationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DEACTIVATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());
        MessageDto deactivationNotification = new MessageDto();

        List<Identity> userList = new ArrayList<>();
        userList.add(deactivationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createSuspensionMessage(deactivationUser)).thenReturn(deactivationNotification);

        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createSuspensionMessage(deactivationUser);
        verify(notificationService, times(1)).send(deactivationNotification);
        verify(identityRepository, times(1)).saveAndFlush(deactivationUser);

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        assertFalse(deactivationUser.isActive());
        assertNull(deactivationUser.getAgencyTokenUid());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_NoDeactivationWhenUserInDeactivationRangeButAlreadyDeactivated() {
        Identity deactivationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DEACTIVATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        List<Identity> userList = new ArrayList<>();
        userList.add(deactivationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);

        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }


    @Test
    public void dataRetentionJob_NotificationWhenUserInNotificationRange() {
        Identity notificationUserDeactivated = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, null);
        Identity notificationUserActive = new Identity(UUID.randomUUID().toString(), "email", "password", USER_ACTIVE, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_NOT_SENT, UUID.randomUUID().toString());

        MessageDto deletionWarningNotification = new MessageDto();

        List<Identity> userList = new ArrayList<>();
        userList.add(notificationUserDeactivated);
        userList.add(notificationUserActive);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createDeletionMessage(notificationUserDeactivated)).thenReturn(deletionWarningNotification);
        when(messageService.createDeletionMessage(notificationUserActive)).thenReturn(deletionWarningNotification);

        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createDeletionMessage(notificationUserActive);
        verify(messageService, times(1)).createDeletionMessage(notificationUserDeactivated);
        verify(notificationService, times(2)).send(deletionWarningNotification);
        verify(identityRepository, times(1)).saveAndFlush(notificationUserDeactivated);
        verify(identityRepository, times(1)).saveAndFlush(notificationUserActive);
        verifyNoMoreInteractions(identityRepository);

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        for (Identity user : userList) {
            assertTrue(user.isDeletionNotificationSent());
        }

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_NoNotificationSentWhenUserInNotificationRangeButNotificationAlreadySent() {
        Identity notificationUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, NOTIFICATION_LLIT, DELETE_NOTIFICATION_SENT, null);

        List<Identity> userList = new ArrayList<>();
        userList.add(notificationUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);

        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_DeletionWhenUserInDeletionRange() {
        Identity deletionUser = new Identity(UUID.randomUUID().toString(), "email", "password", USER_DEACTIVATED, USER_NOT_LOCKED, DEFAULT_ROLE_SET, DELETION_LLIT, DELETE_NOTIFICATION_SENT, null);
        MessageDto deletionNotification = new MessageDto();
        List<Identity> userList = new ArrayList<>();
        userList.add(deletionUser);

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(userList);
        when(messageService.createDeletedMessage(deletionUser)).thenReturn(deletionNotification);
        when(identityRepository.findFirstByUid(deletionUser.getUid())).thenReturn(Optional.of(deletionUser));

        dataRetentionJobService.runDataRetentionJob();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(messageService, times(1)).createDeletedMessage(deletionUser);
        verify(notificationService, times(1)).send(deletionNotification);
        verify(identityRepository, times(1)).findFirstByUid(deletionUser.getUid());
        verify(identityRepository, times(1)).delete(deletionUser);
        verify(identityRepository, times(1)).flush();

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_MixActionWhenUsersInMixRange() {
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
        when(identityRepository.findFirstByUid(deletionUser.getUid())).thenReturn(Optional.of(deletionUser));

        dataRetentionJobService.runDataRetentionJob();

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

        verify(identityRepository, times(1)).findFirstByUid(deletionUser.getUid());
        verify(identityRepository, times(1)).flush();

        verifyNoMoreInteractions(identityRepository, notificationService, messageService, identityService);

        assertFalse(deactivationUser.isActive());
        assertNull(deactivationUser.getAgencyTokenUid());
        assertTrue(notificationUser.isDeletionNotificationSent());

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }

    @Test
    public void dataRetentionJob_NoErrorWhenNoUsers() {
        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(Collections.EMPTY_LIST);
        dataRetentionJobService.runDataRetentionJob();

        verifyZeroInteractions(notificationService, identityService);
        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verifyNoMoreInteractions(identityRepository);

        verify(requestEntityFactory, times(1)).createLogoutRequest();
        verify(restTemplate, times(1)).exchange(requestEntity, Void.class);
        verifyNoMoreInteractions(requestEntityFactory, restTemplate);
    }
}

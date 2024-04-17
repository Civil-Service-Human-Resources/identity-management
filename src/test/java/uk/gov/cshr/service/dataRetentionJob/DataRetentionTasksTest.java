package uk.gov.cshr.service.dataRetentionJob;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.RequestEntityFactory;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeactivationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionNotificationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionTask;
import uk.gov.cshr.service.security.IdentityService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataRetentionTasksTest {

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

    private DeactivationTask getDeactivationTask() {
        return new DeactivationTask(identityRepository, messageService, notificationService);
    }

    private DeletionNotificationTask getDeletionNotificationTask() {
        return new DeletionNotificationTask(identityRepository, messageService, notificationService);
    }

    private DeletionTask getDeletionTask() {
        return new DeletionTask(identityRepository, messageService, notificationService, identityService);
    }

    /*
    * The deactivation job should take the users it gets from the SQL statement and
    * set the active field to false, agencyTokenUid to null and send the relevant email notification
    * */
    @Test
    public void deactivationUpdatesSuccessfullyApplied() {

        List<Identity> usersToReturn = new ArrayList<>();
        Identity deactivationUser = new Identity();
        deactivationUser.setActive(true);
        deactivationUser.setAgencyTokenUid("TEST");
        usersToReturn.add(deactivationUser);

        MessageDto deactivationNotification = new MessageDto();

        when(identityRepository.findByActiveTrueAndLastLoggedInBefore(any())).thenReturn(usersToReturn);
        when(messageService.createSuspensionMessage(deactivationUser)).thenReturn(deactivationNotification);

        DeactivationTask taskToTest = getDeactivationTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByActiveTrueAndLastLoggedInBefore(any());
        verify(identityRepository, times(1)).saveAndFlush(deactivationUser);
        verify(messageService, times(1)).createSuspensionMessage(deactivationUser);
        verify(notificationService, times(1)).send(deactivationNotification);

        assertFalse(deactivationUser.isActive());
        assertEquals("TEST", deactivationUser.getAgencyTokenUid());
    }

    /*
    * The deletion notification job should take the users it gets from the SQL statement and
    * set the deletionNotificationSent field to true, as well as send the relevant email notification
    * */
    @Test
    public void deletionNotificationUpdatesSuccessfullyApplied() {

        List<Identity> usersToReturn = new ArrayList<>();
        Identity deletionNotificationUser = new Identity();
        deletionNotificationUser.setDeletionNotificationSent(false);
        usersToReturn.add(deletionNotificationUser);

        MessageDto deletionNotification = new MessageDto();

        when(identityRepository.findByDeletionNotificationSentFalseAndLastLoggedInBefore(any())).thenReturn(usersToReturn);
        when(messageService.createDeletionMessage(deletionNotificationUser)).thenReturn(deletionNotification);

        DeletionNotificationTask taskToTest = getDeletionNotificationTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByDeletionNotificationSentFalseAndLastLoggedInBefore(any());
        verify(identityRepository, times(1)).saveAndFlush(deletionNotificationUser);
        verify(messageService, times(1)).createDeletionMessage(deletionNotificationUser);
        verify(notificationService, times(1)).send(deletionNotification);

        assertTrue(deletionNotificationUser.isDeletionNotificationSent());
    }

    /*
    * The deletion job should take the users it gets from the SQL statement and
    * call the identityService.deleteIdentity function on each one using the UID.
    * It should also send the relevant email notification.
    * */
    @Test
    public void deletionJobSuccessfulRun() {

        List<Identity> usersToReturn = new ArrayList<>();
        Identity deletionUser = new Identity();
        deletionUser.setUid("TEST");
        usersToReturn.add(deletionUser);

        MessageDto deletedNotification = new MessageDto();

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(usersToReturn);
        when(messageService.createDeletedMessage(deletionUser)).thenReturn(deletedNotification);

        DeletionTask taskToTest = getDeletionTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(identityService, times(1)).deleteIdentity("TEST");
        verify(messageService, times(1)).createDeletedMessage(deletionUser);
        verify(notificationService, times(1)).send(deletedNotification);

    }

    /*
    * Test that the BaseTask update user function carries on even when it hits an exception
    * updating a single user
    * */
    @Test
    public void testTaskContinuationOnUserUpdateFail() {
        List<Identity> usersToReturn = new ArrayList<>();
        Identity firstDeletionUser = new Identity();
        firstDeletionUser.setUid("TEST01");
        usersToReturn.add(firstDeletionUser);

        Identity secondDeletionUser = new Identity();
        secondDeletionUser.setUid("TEST02");
        usersToReturn.add(secondDeletionUser);

        MessageDto deletedNotification = new MessageDto();

        when(identityRepository.findByLastLoggedInBefore(any())).thenReturn(usersToReturn);
        // Simulate the createDeletedMessage method failing on the FIRST user
        when(messageService.createDeletedMessage(any()))
                .thenThrow(RuntimeException.class)
                .thenReturn(deletedNotification);

        DeletionTask taskToTest = getDeletionTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByLastLoggedInBefore(any());
        verify(identityService, times(2)).deleteIdentity(any());
        verify(messageService, times(2)).createDeletedMessage(any());
        // Should only be called ONCE, for the SECOND user
        verify(notificationService, times(1)).send(deletedNotification);
    }

    /*
    * Test that the deletion notification job and deactivation job continue running
    * even when the deletion job fails.
    * */
    public void testJobContinuationWhenOneJobFails() {
        List<Identity> usersToReturn = new ArrayList<>();
        Identity genericUser = new Identity();
        usersToReturn.add(genericUser);

        MessageDto genericNotification = new MessageDto();

        when(identityRepository.findByLastLoggedInBefore(any())).thenThrow(RuntimeException.class);
        when(identityRepository.findByDeletionNotificationSentFalseAndLastLoggedInBefore(any())).thenReturn(usersToReturn);
        when(identityRepository.findByActiveTrueAndLastLoggedInBefore(any())).thenReturn(usersToReturn);

        when(messageService.createDeletionMessage(genericUser)).thenReturn(genericNotification);
        when(messageService.createSuspensionMessage(genericUser)).thenReturn(genericNotification);

        DeactivationTask deactivationTask = getDeactivationTask();
        DeletionTask deletionTask = getDeletionTask();
        DeletionNotificationTask deletionNotificationTask = getDeletionNotificationTask();

        DataRetentionJobService service = new DataRetentionJobService(restTemplate, requestEntityFactory, deactivationTask, deletionNotificationTask, deletionTask);
        service.runDataRetentionJob();

        // Perform a simple validation on the email send function (the last thing to happen in the two tasks that didn't fail)
        verify(notificationService, times(2)).send(genericNotification);
    }
}

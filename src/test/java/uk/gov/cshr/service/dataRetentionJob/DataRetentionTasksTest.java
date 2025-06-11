package uk.gov.cshr.service.dataRetentionJob;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.ReactivationRepository;
import uk.gov.cshr.service.RequestEntityFactory;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeactivationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionNotificationTask;
import uk.gov.cshr.service.dataRetentionJob.tasks.DeletionTask;
import uk.gov.cshr.service.security.IdentityManagementService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataRetentionTasksTest {

    @Mock
    private IdentityRepository identityRepository;
    @Mock
    private ReactivationRepository reactivationRepository;
    @Mock
    private IdentityManagementService identityManagementService;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RequestEntityFactory requestEntityFactory;

    private final Clock clock = Clock.fixed(Instant.parse("2024-01-01T10:00:00.000Z"), ZoneId.of("UTC"));

    private DeactivationTask getDeactivationTask() {
        return new DeactivationTask(clock, identityRepository, reactivationRepository, identityManagementService);
    }

    private DeletionNotificationTask getDeletionNotificationTask() {
        return new DeletionNotificationTask(clock, identityRepository, identityManagementService);
    }

    private DeletionTask getDeletionTask() {
        return getDeletionTask(identityManagementService);
    }

    private DeletionTask getDeletionTask(IdentityManagementService identityManagementService){
        return new DeletionTask(clock, identityRepository, identityManagementService);
    }

    /*
    * The deactivation job should take the users it gets from the SQL statement and
    * set the active field to false, agencyTokenUid to null and send the relevant email notification
    * */
    @Test
    public void deactivationUpdatesSuccessfullyApplied() {

        List<Identity> activeIdentitiesLastLoggedInBeforeDeactivationDate = new ArrayList<>();
        List<Reactivation> reactivationAfterDeactivationDate = new ArrayList<>();

        Identity deactivationUser1 = new Identity();
        deactivationUser1.setEmail("DeactivationUser1@example.com");
        deactivationUser1.setActive(true);
        deactivationUser1.setAgencyTokenUid("TEST");
        activeIdentitiesLastLoggedInBeforeDeactivationDate.add(deactivationUser1);

        Identity deactivationUser2 = new Identity();
        deactivationUser2.setEmail("DeactivationUser2@example.com");
        deactivationUser2.setActive(false);
        deactivationUser2.setAgencyTokenUid("TEST");
        activeIdentitiesLastLoggedInBeforeDeactivationDate.add(deactivationUser2);

        Reactivation reactivation1 = new Reactivation();
        reactivation1.setEmail("deactivationuser2@Example.Com");
        reactivationAfterDeactivationDate.add(reactivation1);

        Reactivation reactivation2 = new Reactivation();
        reactivation2.setEmail("Deactivationuser3@Example.Com");
        reactivationAfterDeactivationDate.add(reactivation2);

        MessageDto deactivationNotification = new MessageDto();

        when(identityRepository.findByActiveTrueAndLastLoggedInBefore(any()))
                .thenReturn(activeIdentitiesLastLoggedInBeforeDeactivationDate);
        when(reactivationRepository.findByReactivatedAtAfter(any()))
                .thenReturn(reactivationAfterDeactivationDate);

        DeactivationTask taskToTest = getDeactivationTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByActiveTrueAndLastLoggedInBefore(any());
        verify(reactivationRepository, times(1)).findByReactivatedAtAfter(any());
        verify(identityManagementService, times(1)).deactivateIdentities(Collections.singletonList(deactivationUser1));
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

        when(identityRepository.findByActiveFalseAndDeletionNotificationSentFalseAndLastLoggedInBefore(any()))
                .thenReturn(usersToReturn);

        DeletionNotificationTask taskToTest = getDeletionNotificationTask();
        taskToTest.runTask();

        verify(identityRepository, times(1))
                .findByActiveFalseAndDeletionNotificationSentFalseAndLastLoggedInBefore(any());
        verify(identityManagementService, times(1)).markUsersForDeletion(usersToReturn);
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

        when(identityRepository.findByActiveFalseAndLastLoggedInBefore(any())).thenReturn(usersToReturn);

        DeletionTask taskToTest = getDeletionTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByActiveFalseAndLastLoggedInBefore(any());
        verify(identityManagementService, times(1)).deleteIdentity(deletionUser);
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

        when(identityRepository.findByActiveFalseAndLastLoggedInBefore(any())).thenReturn(usersToReturn);
        // Simulate the deleteIdentity method failing for the FIRST user
        doThrow(new RuntimeException()).when(identityManagementService).deleteIdentity(firstDeletionUser);

        DeletionTask taskToTest = getDeletionTask();
        taskToTest.runTask();

        verify(identityRepository, times(1)).findByActiveFalseAndLastLoggedInBefore(any());
        // Should be called once
        verify(identityManagementService, times(1)).deleteIdentity(secondDeletionUser);
    }

    /*
    * Test that the deletion notification job and deactivation job continue running
    * even when the deletion job fails.
    * */
    public void testJobContinuationWhenOneJobFails() {
        List<Identity> usersToReturn = new ArrayList<>();
        Identity genericUser = new Identity();
        usersToReturn.add(genericUser);

        // Deletion job
        doThrow(new RuntimeException()).when(identityRepository).findByActiveFalseAndLastLoggedInBefore(any());
        // Deletion notification job
        when(identityRepository.findByActiveFalseAndDeletionNotificationSentFalseAndLastLoggedInBefore(any()))
                .thenReturn(usersToReturn);
        // Deactivation job
        when(identityRepository.findByActiveTrueAndLastLoggedInBefore(any())).thenReturn(usersToReturn);

        DeactivationTask deactivationTask = getDeactivationTask();
        DeletionTask deletionTask = getDeletionTask();
        DeletionNotificationTask deletionNotificationTask = getDeletionNotificationTask();

        DataRetentionJobService service = new DataRetentionJobService(restTemplate, requestEntityFactory,
                deactivationTask, deletionNotificationTask, deletionTask);
        service.runDataRetentionJob();

        verify(identityManagementService, times(0)).deleteIdentity(any());
        verify(identityManagementService, times(1)).markUsersForDeletion(usersToReturn);
        verify(identityManagementService, times(1)).deactivateIdentities(usersToReturn);
    }
}

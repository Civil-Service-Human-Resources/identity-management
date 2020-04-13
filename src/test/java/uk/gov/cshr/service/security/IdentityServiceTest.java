package uk.gov.cshr.service.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.AgencyTokenService;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.ResetService;
import uk.gov.cshr.service.TokenService;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

@SpringBootTest
@RunWith(SpringRunner.class)
public class IdentityServiceTest {

  private static final int DEACTIVATION_WINDOW = 1;
  private static final int DEACTIVATION_OFFSET = 3;
  private static int NOTIFICATION_WINDOW = 5;
  private static int NOTIFICATION_OFFSET = 7;
  private static int DELETION_WINDOW = 10;
  private static int DELETION_OFFSET = 12;

  @Autowired
  IdentityService identityService;

  @MockBean
  IdentityRepository identityRepository;

  @MockBean
  NotificationService notificationService;

  @MockBean
  LearnerRecordService learnerRecordService;

  @MockBean
  CSRSService csrsService;

  @MockBean
  InviteService inviteService;

  @MockBean
  ResetService resetService;

  @MockBean
  TokenService tokenService;

  @MockBean
  AgencyTokenService agencyTokenService;

  @MockBean
  MessageService messageService;

  @Before
  public void setUp() {
    // Overwrite values so we can ignore config
    ReflectionTestUtils.setField(identityService, "deactivationMonths", DEACTIVATION_WINDOW);
    ReflectionTestUtils.setField(identityService, "notificationMonths", NOTIFICATION_WINDOW);
    ReflectionTestUtils.setField(identityService, "deletionMonths", DELETION_WINDOW);
  }

  @Test
  public void trackUserActivity_verifyNoActionsTakenOnActiveUser() {

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(1, 0, true, false, false, false));

    when(identityRepository.findAll()).thenReturn(identities);

    identityService.trackUserActivity();

    verify(identityRepository, times(1)).findAll();
    verifyNoMoreInteractions(identityRepository);

    verifyZeroInteractions(notificationService);
    verifyZeroInteractions(messageService);
    verifyZeroInteractions(csrsService);
    verifyZeroInteractions(learnerRecordService);
    verifyZeroInteractions(inviteService);
    verifyZeroInteractions(resetService);
    verifyZeroInteractions(tokenService);
  }

  @Test
  public void trackUserActivity_verifyDeactivationOfUserInDeactivationTimeFrame() {

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(1, DEACTIVATION_OFFSET, true, false, false, false));

    MessageDto suspensionMessage = new MessageDto();

    when(identityRepository.findAll()).thenReturn(identities);
    when(messageService.createSuspensionMessage(identities.get(0))).thenReturn(suspensionMessage);

    assertTrue(identities.get(0).isActive());

    identityService.trackUserActivity();

    assertFalse(identities.get(0).isActive());

    verify(identityRepository, times(1)).findAll();
    verify(messageService, times(1)).createSuspensionMessage(identities.get(0));
    verify(notificationService, times(1)).send(suspensionMessage);
    verify(agencyTokenService, times(1)).updateAgencyTokenQuotaForUser(identities.get(0), true);
    verify(csrsService, times(1)).removeOrg();
    verify(identityRepository, times(1)).save(identities.get(0));

    verifyNoMoreInteractions(identityRepository);
    verifyNoMoreInteractions(messageService);
    verifyNoMoreInteractions(notificationService);
    verifyNoMoreInteractions(agencyTokenService);
    verifyNoMoreInteractions(csrsService);

    verifyZeroInteractions(learnerRecordService);
    verifyZeroInteractions(inviteService);
    verifyZeroInteractions(resetService);
    verifyZeroInteractions(tokenService);
  }

  @Test
  public void trackUserActivity_verifyNoDeactivationOfUserInDeactivationTimeFrameWhenAlreadyDeactivated() {

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(1, DEACTIVATION_OFFSET, false, false, false, false));

    MessageDto suspensionMessage = new MessageDto();

    when(identityRepository.findAll()).thenReturn(identities);
    when(messageService.createSuspensionMessage(identities.get(0))).thenReturn(suspensionMessage);

    identityService.trackUserActivity();

    verify(identityRepository, times(1)).findAll();
    verifyNoMoreInteractions(identityRepository);

    verifyZeroInteractions(notificationService);
    verifyZeroInteractions(messageService);
    verifyZeroInteractions(csrsService);
    verifyZeroInteractions(learnerRecordService);
    verifyZeroInteractions(inviteService);
    verifyZeroInteractions(resetService);
    verifyZeroInteractions(tokenService);
  }

  @Test
  public void trackUserActivity_verifyNotificationOfUserInNotificationTimeFrame() {

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(1, NOTIFICATION_OFFSET, false, false, false, false));

    MessageDto notificationMessage = new MessageDto();

    when(identityRepository.findAll()).thenReturn(identities);
    when(messageService.createDeletionMessage(identities.get(0))).thenReturn(notificationMessage);

    assertFalse(identities.get(0).isDeletionNotificationSent());

    identityService.trackUserActivity();

    assertTrue(identities.get(0).isDeletionNotificationSent());

    verify(identityRepository, times(1)).findAll();
    verify(messageService, times(1)).createDeletionMessage(identities.get(0));
    verify(notificationService, times(1)).send(notificationMessage);
    verify(identityRepository, times(1)).save(identities.get(0));

    verifyNoMoreInteractions(identityRepository);
    verifyNoMoreInteractions(messageService);
    verifyNoMoreInteractions(notificationService);

    verifyZeroInteractions(csrsService);
    verifyZeroInteractions(agencyTokenService);
    verifyZeroInteractions(learnerRecordService);
    verifyZeroInteractions(inviteService);
    verifyZeroInteractions(resetService);
    verifyZeroInteractions(tokenService);
  }

  @Test
  public void trackUserActivity_verifyNoNotificationOfUserInNotificationTimeFrameWhenNotificationAlreadySent() {

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(1, NOTIFICATION_OFFSET, false, true, false, false));

    MessageDto notificationMessage = new MessageDto();

    when(identityRepository.findAll()).thenReturn(identities);
    when(messageService.createDeletionMessage(identities.get(0))).thenReturn(notificationMessage);

    identityService.trackUserActivity();

    verify(identityRepository, times(1)).findAll();
    verifyNoMoreInteractions(identityRepository);

    verifyZeroInteractions(notificationService);
    verifyZeroInteractions(messageService);
    verifyZeroInteractions(csrsService);
    verifyZeroInteractions(learnerRecordService);
    verifyZeroInteractions(inviteService);
    verifyZeroInteractions(resetService);
    verifyZeroInteractions(tokenService);
  }

  @Test
  public void trackUserActivity_verifyDeletionOfUserInDeletionTimeFrame() {

    long userId = new Random().nextLong();

    List<Identity> identities = new ArrayList<>();
    identities.add(createRandomIdentityWithOffsetLastLoggedIn(userId, DELETION_OFFSET, false, true, false, false));

    MessageDto deletionMessage = new MessageDto();

    when(identityRepository.findAll()).thenReturn(identities);
    when(messageService.createDeletedMessage(identities.get(0))).thenReturn(deletionMessage);
    when(learnerRecordService.deleteCivilServant(identities.get(0).getUid())).thenReturn(ResponseEntity.noContent().build());
    when(csrsService.deleteCivilServant(identities.get(0).getUid())).thenReturn(ResponseEntity.noContent().build());
    when(identityRepository.findFirstByUid(identities.get(0).getUid())).thenReturn(Optional.of(identities.get(0)));

    identityService.trackUserActivity();

    verify(identityRepository, times(1)).findAll();
    verify(messageService, times(1)).createDeletedMessage(identities.get(0));
    verify(notificationService, times(1)).send(deletionMessage);
    verify(learnerRecordService, times(1)).deleteCivilServant(identities.get(0).getUid());
    verify(csrsService, times(1)).deleteCivilServant(identities.get(0).getUid());
    verify(identityRepository, times(1)).findFirstByUid(identities.get(0).getUid());
    verify(inviteService, times(1)).deleteInvitesByIdentity(identities.get(0));
    verify(resetService, times(1)).deleteResetsByIdentity(identities.get(0));
    verify(tokenService, times(1)).deleteTokensByIdentity(identities.get(0));
    verify(identityRepository, times(1)).delete(identities.get(0));
    verify(identityRepository, times(1)).delete(identities.get(0));

    verifyNoMoreInteractions(identityRepository);
    verifyNoMoreInteractions(messageService);
    verifyNoMoreInteractions(notificationService);
    verifyNoMoreInteractions(inviteService);
    verifyNoMoreInteractions(resetService);
    verifyNoMoreInteractions(tokenService);
    verifyNoMoreInteractions(csrsService);
    verifyNoMoreInteractions(learnerRecordService);

    verifyZeroInteractions(agencyTokenService);

  }

  private Identity createRandomIdentityWithOffsetLastLoggedIn(long userId, int offset, boolean isActive, boolean deleteNotificationSent, boolean isEmailRecentlyUpdated, boolean isRecentlyReactivated) {
    return new Identity(String.valueOf(userId), null, null, isActive, false, null,
        LocalDateTime.now().minusMonths(offset).toInstant(ZoneOffset.UTC), deleteNotificationSent, isEmailRecentlyUpdated, isRecentlyReactivated);
  }
}
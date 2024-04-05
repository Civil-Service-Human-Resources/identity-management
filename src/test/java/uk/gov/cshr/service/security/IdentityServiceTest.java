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
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.*;
import uk.gov.cshr.service.learnerRecord.LearnerRecordService;

import java.util.*;

import static org.junit.Assert.*;
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
    private LearnerRecordService learnerRecordService;
    @Mock
    private CSRSService csrsService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RequestEntityFactory requestEntityFactory;
    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    private IdentityService identityService;

    private final RequestEntity requestEntity = RequestEntity.get(null).build();
    private final ResponseEntity responseEntity = ResponseEntity.ok().build();

    @Before
    public void createIdentityService() {
        identityService = new IdentityService(identityRepository, learnerRecordService, csrsService, resetService, restTemplate, requestEntityFactory);
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
        assertFalse(actualIdentity.isLocked());
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
        assertTrue(actualIdentity.isLocked());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundIfIdentityDoesNotExistWhenUpdatedLocked() {
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        identityService.updateLocked(UID);

        verify(identityRepository, times(0)).save(any(Identity.class));
    }

}

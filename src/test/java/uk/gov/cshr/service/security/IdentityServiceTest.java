package uk.gov.cshr.service.security;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.cshr.client.identity.IIdentityAPIClient;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdentityServiceTest {

    private static final String UID = "UID";
    private static final Long ID = 1L;

    @Mock
    private IdentityRepository identityRepository;
    @Mock
    private IIdentityAPIClient identityClient;
    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);

    @InjectMocks
    private IdentityService identityService;

    @Test
    public void shouldGetIdentity() {
        Identity identity = new Identity();
        identity.setId(ID);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        Identity actualIdentity = identityService.getIdentity(UID);

        assertEquals(ID, actualIdentity.getId());
    }

    @Test
    public void shouldSetLockedToFalseIfLockedIsTrue() {
        Identity identity = new Identity();
        identity.setLocked(true);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));

        when(identityRepository.save(identity)).thenReturn(identity);

        identityService.updateLockStatus(UID);

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

        identityService.updateLockStatus(UID);

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertTrue(actualIdentity.isLocked());
    }

    @Test
    public void shouldThrowResourceNotFoundIfIdentityDoesNotExistWhenUpdatedLocked() {
        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> identityService.updateLockStatus(UID));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }
}

package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest
public class AgencyTokenServiceTest {

    @MockBean
    private CSRSService csrsService;

    @Autowired
    private AgencyTokenService classUnderTest;

    private Identity identity;

    @Before
    public void setUp() {
        identity = buildIdentity();

        ResponseEntity getOrgCodeResponseEntity = new ResponseEntity("co", HttpStatus.OK);
        when(csrsService.getOrganisationCodeForCivilServant(eq(identity.getUid()))).thenReturn(getOrgCodeResponseEntity);

        AgencyTokenResponseDTO agencyTokenResponseDTO = new AgencyTokenResponseDTO();
        agencyTokenResponseDTO.setCapacity(100);
        agencyTokenResponseDTO.setCapacityUsed(20);
        agencyTokenResponseDTO.setToken("aToken");
        agencyTokenResponseDTO.setId(new Long(1));
        ResponseEntity getAgencyTokenResponseEntity = new ResponseEntity(agencyTokenResponseDTO, HttpStatus.OK);
        when(csrsService.getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"))).thenReturn(getAgencyTokenResponseEntity);

        AgencyTokenResponseDTO updateAgencyTokenResponseDTO = new AgencyTokenResponseDTO();
        updateAgencyTokenResponseDTO.setCapacity(100);
        updateAgencyTokenResponseDTO.setCapacityUsed(21);
        updateAgencyTokenResponseDTO.setToken("aToken");
        updateAgencyTokenResponseDTO.setId(new Long(1));
        ResponseEntity updateAgencyTokenResponseEntity = new ResponseEntity(updateAgencyTokenResponseDTO, HttpStatus.OK);
        when(csrsService.updateAgencyTokenForCivilServant(eq("co"), eq(identity.getEmail()), eq("aToken"), anyBoolean())).thenReturn(updateAgencyTokenResponseEntity);
    }

    @Test
    public void givenAValidIdentityAndRemoveUserIsTrue_whenUpdateAgencyTokenUsageForUser_thenReturnsSuccessfully() {

        boolean actual = classUnderTest.updateAgencyTokenQuotaForUser(identity, true);

        assertTrue(actual);

        verify(csrsService, times(1)).getOrganisationCodeForCivilServant(eq(identity.getUid()));
        verify(csrsService, times(1)).getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"));
        verify(csrsService, times(1)).updateAgencyTokenForCivilServant(eq("co"), eq(identity.getEmail()), eq("aToken"), eq(true));
    }

    @Test
    public void givenAValidIdentityAndRemoveUserIsFalse_whenUpdateAgencyTokenUsageForUser_thenReturnsSuccessfully() {

        boolean actual = classUnderTest.updateAgencyTokenQuotaForUser(identity, false);

        assertTrue(actual);

        verify(csrsService, times(1)).getOrganisationCodeForCivilServant(eq(identity.getUid()));
        verify(csrsService, times(1)).getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"));
        verify(csrsService, times(1)).updateAgencyTokenForCivilServant(eq("co"), eq(identity.getEmail()), eq("aToken"), eq(false));
    }

    @Test
    public void givenANotFoundOrgCode_whenUpdateAgencyTokenUsageForUser_thenReturnsFalseAndRollsback() {

        ResponseEntity getOrgCodeResponseEntity = new ResponseEntity(HttpStatus.NOT_FOUND);
        when(csrsService.getOrganisationCodeForCivilServant(eq(identity.getUid()))).thenReturn(getOrgCodeResponseEntity);

        boolean actual = classUnderTest.updateAgencyTokenQuotaForUser(identity, true);

        assertFalse(actual);

        verify(csrsService, times(1)).getOrganisationCodeForCivilServant(eq(identity.getUid()));
        verify(csrsService, never()).getAgencyTokenForCivilServant((anyString()), anyString());
        verify(csrsService, never()).updateAgencyTokenForCivilServant(anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    public void givenANotFoundAgencyToken_whenUpdateAgencyTokenUsageForUser_thenReturnsFalseAndRollsback() {

        ResponseEntity getAgencyTokenResponseEntity = new ResponseEntity(HttpStatus.NOT_FOUND);
        when(csrsService.getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"))).thenReturn(getAgencyTokenResponseEntity);

        boolean actual = classUnderTest.updateAgencyTokenQuotaForUser(identity, true);

        assertFalse(actual);

        verify(csrsService, times(1)).getOrganisationCodeForCivilServant(eq(identity.getUid()));
        verify(csrsService, times(1)).getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"));
        verify(csrsService, never()).updateAgencyTokenForCivilServant(anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    public void givenAgencyTokenQutoaNotUpdated_whenUpdateAgencyTokenUsageForUser_thenReturnsFalseAndRollsback() {

        ResponseEntity updateAgencyTokenResponseEntity = new ResponseEntity(HttpStatus.CONFLICT);
        when(csrsService.updateAgencyTokenForCivilServant(eq("co"), eq(identity.getEmail()), eq("aToken"), eq(true))).thenReturn(updateAgencyTokenResponseEntity);

        boolean actual = classUnderTest.updateAgencyTokenQuotaForUser(identity, true);

        assertFalse(actual);

        verify(csrsService, times(1)).getOrganisationCodeForCivilServant(eq(identity.getUid()));
        verify(csrsService, times(1)).getAgencyTokenForCivilServant(eq(identity.getEmail()), eq("co"));
        verify(csrsService, times(1)).updateAgencyTokenForCivilServant(eq("co"), eq(identity.getEmail()), eq("aToken"), eq(true));
    }

    private Identity buildIdentity() {
        Set<Role> roles = new HashSet<Role>();
        roles.add(new Role());
        return new Identity("aUid", "someone@example.com", "myPassword", true, false, roles, Instant.now(), false, UUID.randomUUID().toString());
    }

}

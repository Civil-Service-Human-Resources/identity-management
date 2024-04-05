package uk.gov.cshr.controller;

import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(IdentityController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, IdentityController.class})
@EnableSpringDataWebSupport
public class IdentityControllerTest {

    private static final String UID = "UID";
    private static final String AGENCY_UID = "AGENCY_UID";
    private static final String IDENTITIES_URL = "/identities";
    private static final String IDENTITIES_REACTIVATE_URL = "/identities/reactivate/";
    private static final String EMAIL = "test@example.com";
    private static final String CODE = "CODE";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityRepository identityRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private IdentityService identityService;

    @MockBean
    private ReactivationService reactivationService;

    @Captor
    private ArgumentCaptor<Identity> identityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        identityArgumentCaptor = ArgumentCaptor.forClass(Identity.class);
    }

    @Test
    public void updateActiveShouldSetActiveToFalse() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        when(identityService.getIdentity(UID)).thenReturn(identity);

        mockMvc.perform(
                        post("/identities/active")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(model().attributeDoesNotExist("status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("success", EMAIL + " deactivated successfully"))
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertEquals(false, actualIdentity.isActive());
        assertEquals(null, actualIdentity.getAgencyTokenUid());
    }

    @Test
    public void updateActiveShouldRedirectToReactivateConfirmationIfNotActive() throws Exception {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        when(identityService.getIdentity(UID)).thenReturn(identity);

        mockMvc.perform(
                        post("/identities/active")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(model().attributeDoesNotExist("status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_REACTIVATE_URL + UID));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void updateActiveShouldThrowResourceNotFound() throws Exception {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new ResourceNotFoundException()).when(identityService).getIdentity(UID);

        mockMvc.perform(
                        post("/identities/active")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(model().attributeDoesNotExist("success"))
                .andExpect(flash().attribute("status", ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }


    @Test
    public void updateActiveShouldThrowGeneralException() throws Exception {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new RuntimeException()).when(identityService).getIdentity(UID);

        mockMvc.perform(
                        post("/identities/active")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(model().attributeDoesNotExist("success"))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void shouldGetReactivateUserConfirmation() throws Exception {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        when(identityService.getIdentity(UID)).thenReturn(identity);

        mockMvc.perform(
                        get("/identities/reactivate/" + UID))
                .andExpect(model().attribute("identity", identity))
                .andExpect(model().attribute("uid", UID))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("identity/reactivate"));
    }

    @Test
    public void shouldGetSendReactivationRequest() throws Exception {
        Identity identity = new Identity();
        identity.setActive(false);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        Reactivation reactivation = new Reactivation();
        reactivation.setEmail(EMAIL);
        reactivation.setCode(CODE);

        when(identityService.getIdentity(UID)).thenReturn(identity);
        when(reactivationService.createReactivationRequest(EMAIL)).thenReturn(reactivation);

        doNothing().when(reactivationService).sendReactivationEmail(identity, reactivation);

        mockMvc.perform(
                        post("/identities/reactivate/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("success", "Reactivation email verification sent to " + EMAIL))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldGetRedirectIfUserAlreadyActiveAndReactivationRequested() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        when(identityService.getIdentity(UID)).thenReturn(identity);

        mockMvc.perform(
                        post("/identities/reactivate/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.IDENTITY_ALREADY_ACTIVE_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldHandleResourceNotFoundIfIdentityNotFound() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new ResourceNotFoundException()).when(identityService).getIdentity(UID);

        mockMvc.perform(
                        post("/identities/reactivate/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldExceptionIfTechnicalExceptionOccurs() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new RuntimeException()).when(identityService).getIdentity(UID);

        mockMvc.perform(
                        post("/identities/reactivate/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldUpdateLocked() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doNothing().when(identityService).updateLocked(UID);

        mockMvc.perform(
                        post("/identities/locked/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldHandleExceptionIfIdentityNotFoundWhenUpdatingLocked() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new ResourceNotFoundException()).when(identityService).updateLocked(UID);

        mockMvc.perform(
                        post("/identities/locked/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void shouldHandleExceptionIfTechnicalExceptionOccursWhenUpdatingLocked() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);

        doThrow(new RuntimeException()).when(identityService).updateLocked(UID);

        mockMvc.perform(
                        post("/identities/locked/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));
    }

    @Test
    public void updateIdentityRoles() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);
        identity.setRoles(null);

        Role learnerRole = new Role("LEARNER", "LEARNER DESC");
        Role adminRole = new Role("ADMIN", "ADMIN DESC");

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));
        when(roleRepository.findById(1)).thenReturn(Optional.of(learnerRole));
        when(roleRepository.findById(2)).thenReturn(Optional.of(adminRole));

        mockMvc.perform(
                        post("/identities/update/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID)
                                .accept(APPLICATION_JSON).param("roleId", "1")
                                .accept(APPLICATION_JSON).param("roleId", "2"))
                .andExpect(model().attributeDoesNotExist("status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository).save(identityArgumentCaptor.capture());

        Set<Role> rolesSet = new HashSet<>(Arrays.asList(learnerRole, adminRole));

        Identity actualIdentity = identityArgumentCaptor.getValue();
        assertEquals(true, actualIdentity.isActive());
        assertEquals(AGENCY_UID, actualIdentity.getAgencyTokenUid());
        assertEquals(rolesSet, actualIdentity.getRoles());
    }

    @Test
    public void updateIdentityRolesShouldRedirectIfRoleNotPresent() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);
        identity.setRoles(null);

        Role learnerRole = new Role("LEARNER", "LEARNER DESC");
        Role adminRole = new Role("ADMIN", "ADMIN DESC");

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.of(identity));
        when(roleRepository.findById(1)).thenReturn(Optional.of(learnerRole));
        when(roleRepository.findById(2)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(3)).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/identities/update/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID)
                                .accept(APPLICATION_JSON).param("roleId", "1")
                                .accept(APPLICATION_JSON).param("roleId", "2")
                                .accept(APPLICATION_JSON).param("roleId", "3"))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void updateIdentityRolesShouldRedirectIfIdentityNotPresent() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);
        identity.setRoles(null);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/identities/update/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID)
                                .accept(APPLICATION_JSON).param("roleId", "1")
                                .accept(APPLICATION_JSON).param("roleId", "2")
                                .accept(APPLICATION_JSON).param("roleId", "3"))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }

    @Test
    public void updateIdentityRolesShouldRedirectIfRoleParamNotPresent() throws Exception {
        Identity identity = new Identity();
        identity.setActive(true);
        identity.setEmail(EMAIL);
        identity.setAgencyTokenUid(AGENCY_UID);
        identity.setRoles(null);

        when(identityRepository.findFirstByUid(UID)).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/identities/update/")
                                .with(csrf())
                                .accept(APPLICATION_JSON).param("uid", UID))
                .andExpect(flash().attribute("status", ApplicationConstants.SYSTEM_ERROR))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDENTITIES_URL));

        verify(identityRepository, times(0)).save(any(Identity.class));
    }
}

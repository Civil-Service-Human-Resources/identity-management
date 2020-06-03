package uk.gov.cshr.controller;

import org.glassfish.jersey.servlet.WebConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;

import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(IdentityController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, IdentityController.class})
@EnableSpringDataWebSupport
public class IdentityControllerTest {

    private static final String UID = "UID";

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

    @Test
    public void updateActive() throws Exception {
        doNothing().when(identityService).updateActive(UID);

        mockMvc.perform(
                post("/identities/active")
                        .with(csrf())
                        .accept(APPLICATION_JSON).param("uid", UID))
                .andDo(print())
                .andExpect(model().attributeDoesNotExist("status"))
                .andExpect(status().is3xxRedirection());
    }
}
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
import uk.gov.cshr.service.security.IdentityService;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(UserController.class)
@WithMockUser(username = "user")
@ContextConfiguration(classes = {WebConfig.class, UserController.class})
@EnableSpringDataWebSupport
public class UserControllerTest {

    private static final String IDM_LOGIN_URL = "http://localhost:8080/logout?returnTo=http://localhost:8081/mgmt/login";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityService identityService;

    @Test
    public void userShouldLogoutAndRedirectedToIDMLoginPage() throws Exception {
        doNothing().when(identityService).logoutUser();

        mockMvc.perform(
                get("/sign-out"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(IDM_LOGIN_URL));

    }
}
package uk.gov.cshr.controller;

import org.glassfish.jersey.servlet.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.cshr.config.CustomPermissionEvaluator;
import uk.gov.cshr.config.MethodSecurityConfig;
import uk.gov.cshr.utils.CustomOAuth2AuthenticationProvider;

import java.util.Collections;
import java.util.HashSet;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static uk.gov.cshr.utils.AuthUtils.getOAuth2User;

@ContextConfiguration(classes = {WebConfig.class, MethodSecurityConfig.class, CustomOAuth2AuthenticationProvider.class,
        CustomPermissionEvaluator.class})
@EnableSpringDataWebSupport
@ActiveProfiles({"h2"})
public class ControllerTestBase {
    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext context;

    @BeforeEach
    public void setup() {
        RequestPostProcessor identityManageIdentityPostProcessor = authentication(getOAuth2User(new HashSet<>(Collections.singletonList("IDENTITY_MANAGE_IDENTITY"))));
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .defaultRequest(get("/").with(identityManageIdentityPostProcessor))
                .defaultRequest(post("/").with(identityManageIdentityPostProcessor))
                .defaultRequest(put("/").with(identityManageIdentityPostProcessor))
                .alwaysDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .build();
    }

}

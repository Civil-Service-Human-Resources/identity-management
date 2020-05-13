package uk.gov.cshr.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.dto.OrganisationDto;
import uk.gov.cshr.service.organisation.ReportingPermissionService;
import uk.gov.cshr.service.security.IdentityService;

import javax.transaction.Transactional;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReportingPermissionControllerTest {

    @InjectMocks
    private ReportingPermissionController organisationController;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ReportingPermissionService organisationService;

    @Mock
    private IdentityService identityService;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(organisationController).build();
    }

    @Test
    public void shouldReturnOrganisationAddPage() throws Exception {
        when(organisationService.getOrganisations()).thenReturn(new ArrayList<OrganisationDto>());
        this.mockMvc.perform(get("/reportingpermission/add"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }
}

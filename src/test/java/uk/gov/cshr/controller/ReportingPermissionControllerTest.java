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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void shouldAddOrganisationReportingPermission() throws Exception {
        String uid = "test";
        boolean response = true;
        when(identityService.getUIDFromEmail(anyString())).thenReturn(uid);
        when(organisationService.addOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/reportingpermission");
    }

    @Test
    public void shouldUpdateReportingPermission() throws Exception {
        boolean response = true;
        when(organisationService.updateOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/update"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/reportingpermission");
    }

    @Test
    public void shouldDisplayError_WhenUpdateReportingPermission() throws Exception {
        boolean response = false;
        when(organisationService.updateOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/update"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/error");
    }
}

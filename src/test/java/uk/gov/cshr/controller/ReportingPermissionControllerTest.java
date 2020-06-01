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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.OrganisationDto;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.organisation.ReportingPermissionService;
import uk.gov.cshr.service.security.IdentityService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
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
    private ReportingPermissionService reportingPermissionService;

    @Mock
    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(organisationController)
                .setCustomArgumentResolvers(pageableArgumentResolver).build();
    }

    @Test
    public void shouldReturnOrganisationAddPage() throws Exception {
        when(reportingPermissionService.getOrganisations()).thenReturn(new ArrayList<OrganisationDto>());
        this.mockMvc.perform(get("/reportingpermission/add"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void shouldAddOrganisationReportingPermission() throws Exception {
        String uid = "test";
        boolean response = true;
        when(identityService.getUIDFromEmail(anyString())).thenReturn(uid);
        when(reportingPermissionService.addOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/reportingpermission");
    }

    @Test
    public void shouldUpdateReportingPermission() throws Exception {
        boolean response = true;
        when(reportingPermissionService.updateOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/update"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/reportingpermission");
    }

    @Test
    public void shouldDisplayError_WhenUpdateReportingPermission() throws Exception {
        boolean response = false;
        when(reportingPermissionService.updateOrganisationReportingPermission(anyString(), anyList()))
                .thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/update"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/error");
    }

    @Test
    public void shouldShowDeleteReportingPermission() throws Exception {
        String uid = "uid";
        Identity identity = new Identity();
        Optional<Identity> optionalIdentity = Optional.of(identity);
        when(identityRepository.findFirstByUid(uid)).thenReturn(optionalIdentity);
        this.mockMvc.perform(post("/reportingpermission/delete/uid"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("reportingpermission/delete");
    }

    @Test
    public void shouldDeleteReportingPermission() throws Exception {
    Boolean response = true;
    when(reportingPermissionService.deleteOrganisationReportingPermission("uid")).thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/delete"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/reportingpermission");
    }

    @Test
    public void shouldGiveError_WhenDeleteReportingPermission() throws Exception {
        Boolean response = false;
        when(reportingPermissionService.deleteOrganisationReportingPermission("uid")).thenReturn(response);
        this.mockMvc.perform(post("/reportingpermission/delete"))
                .andReturn().getResponse()
                .getContentAsString().equalsIgnoreCase("redirect:/error");
    }

    @Test
    public void shouldListUserWithReportingPermissionWithoutQuery() throws Exception {
        List<Identity> listIdentity = new ArrayList<>();
        Identity identity = new Identity();
        listIdentity.add(identity);
        List<String> listCivilServantUid = new ArrayList<>();
        listCivilServantUid.add("uid1");
        listCivilServantUid.add("uid2");
        Page<Identity> listUser = new PageImpl<>(listIdentity);
        when(reportingPermissionService.getCivilServantUIDsWithReportingPermission())
                .thenReturn(listCivilServantUid);
        when(identityService.getAllIdentityFromUid(any(), anyList()))
                .thenReturn(listUser);
        this.mockMvc.perform(get("/reportingpermission"))
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("reportingpermission/list");
        verify(identityService).getAllIdentityFromUid(any(), anyList());
    }

    @Test
    public void shouldListUserWithReportingPermissionWithQuery() throws Exception {
        List<String> listCivilServantUid = new ArrayList<>();
        listCivilServantUid.add("uid1");
        listCivilServantUid.add("uid2");
        when(reportingPermissionService.getCivilServantUIDsWithReportingPermission())
                .thenReturn(listCivilServantUid);
        this.mockMvc.perform(get("/reportingpermission")
                .param("query", "somestring"))
                .andReturn()
                .getResponse()
                .getContentAsString().equalsIgnoreCase("reportingpermission/list");
        verify(identityService).findAllByForEmailContains("somestring");
    }
}
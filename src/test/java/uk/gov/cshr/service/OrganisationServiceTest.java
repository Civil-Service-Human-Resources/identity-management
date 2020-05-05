package uk.gov.cshr.service;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.cshr.service.organisation.OrganisationDto;
import uk.gov.cshr.service.organisation.OrganisationService;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class OrganisationServiceTest {

    private CSRSService csrsService = mock(CSRSService.class);

    private OrganisationService organisationService = new OrganisationService(csrsService);

    @Test
    public void shouldGetOrganisationReportingPermission() {
        List<Map<String, Object>> listDto = new ArrayList<>();
        Map<String, Object> dtoValue = new HashMap();
        dtoValue.put("href", "http://localhost/organisationunits/1");
        dtoValue.put("name", "user");
        listDto.add(dtoValue);
        ResponseEntity responseEntity = new ResponseEntity<List<Map<String, Object>>>(listDto, HttpStatus.OK);
        when(csrsService.getOrganisations()).thenReturn(responseEntity);

        List<OrganisationDto> response = organisationService.getOrganisations();
        assertTrue(response.size() == 1);
    }

    @Test
    public void shouldAddOrganisationReportingPermission() {
        ResponseEntity responseEntity = new ResponseEntity<String>("true", HttpStatus.OK);
        String uid = "uid";
        List<String> ids = Arrays.asList("1");
        when(csrsService.addOrganisationReportingPermission(uid, ids)).thenReturn(responseEntity);

        boolean response = organisationService.addOrganisationReportingPermission(uid, ids);
        assertTrue(response);
    }

    @Test
    public void shouldNotAddOrganisationReportingPermission() {
        ResponseEntity responseEntity = new ResponseEntity<String>("false", HttpStatus.INTERNAL_SERVER_ERROR);
        String uid = "uid";
        List<String> ids = Arrays.asList("1");
        when(csrsService.addOrganisationReportingPermission(uid, ids)).thenReturn(responseEntity);

        boolean response = organisationService.addOrganisationReportingPermission(uid, ids);
        assertFalse(response);
    }
}
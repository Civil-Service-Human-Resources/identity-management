package uk.gov.cshr.service.organisation;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.dto.OrganisationDto;
import uk.gov.cshr.dto.ReportingPermissionDto;
import uk.gov.cshr.service.CSRSService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ReportingPermissionService {

    private final CSRSService csrsService;

    public ReportingPermissionService(CSRSService csrsService) {
        this.csrsService = csrsService;
    }

    public List<String> getCivilServantUIDsWithReportingPermission() {
        List<String> listUid = new ArrayList<>();
        ResponseEntity response = csrsService.getCivilServantUIDsWithReportingPermission();
        if(response != null && response.getBody() != null) {
            listUid.addAll((ArrayList)response.getBody());
        }
        return listUid;
    }

    public List<OrganisationDto> getOrganisations() {
        return convertToOrganisationDto(csrsService.getOrganisations());
    }

    private List<OrganisationDto> convertToOrganisationDto(ResponseEntity response) {
        List<OrganisationDto> organisationDtoList = new ArrayList<>();
        if(response != null && response.getBody() != null) {
            List<HashMap> listMap = new ArrayList<>();
            listMap = (List<HashMap>)response.getBody();
            for (HashMap res : listMap){
                OrganisationDto dto = new OrganisationDto();
                dto.setId(Integer.parseInt(StringUtils.substringAfterLast(((String)res.get("href")), "/")));
                dto.setName((String) res.get("formattedName"));
                organisationDtoList.add(dto);
            }
        }
        return organisationDtoList;
    }

    public boolean addOrganisationReportingPermission(String uid, List<String> organisationIds) {
        ResponseEntity response = csrsService.addOrganisationReportingPermission(uid, organisationIds);
        return response.getStatusCode().is2xxSuccessful();
    }

    public List<String> getCivilServantReportingPermission(String uid) {
        List<String> listUid = new ArrayList<>();
        listUid.addAll((ArrayList)csrsService.getCivilServantReportingPermission(uid).getBody());
        return listUid;
     }

    public boolean updateOrganisationReportingPermission(String uid, List<String> listOrganisationId) {
        ResponseEntity response = csrsService.updateOrganisationReportingPermission(uid, listOrganisationId);
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean deleteOrganisationReportingPermission(String uid) {
        ResponseEntity response = csrsService.deleteOrganisationReportingPermission(uid);
        return response.getStatusCode().is2xxSuccessful();
    }
}
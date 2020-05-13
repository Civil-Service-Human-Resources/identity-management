package uk.gov.cshr.service.organisation;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.dto.OrganisationDto;
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
        ResponseEntity response = csrsService.getOrganisations();
        return convertToOrganisationDto(response);
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
}
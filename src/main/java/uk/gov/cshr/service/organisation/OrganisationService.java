package uk.gov.cshr.service.organisation;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.notifications.service.MessageService;
import uk.gov.cshr.notifications.service.NotificationService;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.service.CSRSService;

import java.util.*;

@Service
@Transactional
public class OrganisationService {

    private final CSRSService csrsService;

    public OrganisationService(CSRSService csrsService) {
        this.csrsService = csrsService;
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
                dto.setId((Integer) res.get("id"));
                dto.setName((String) res.get("name"));
                organisationDtoList.add(dto);
            }
        }
        return organisationDtoList;
    }

    public boolean addOrganisationReportingPermission(String uid, List<String> organisationIds) {
        ResponseEntity response = csrsService.addOrganisationReportingPermission(uid, organisationIds);
        return response.getStatusCode().is2xxSuccessful() == true ? true: false;
    }
}
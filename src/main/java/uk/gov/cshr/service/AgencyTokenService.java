package uk.gov.cshr.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;

@Service
public class AgencyTokenService {

    private final CSRSService csrsService;

    public AgencyTokenService(CSRSService csrsService) {
        this.csrsService = csrsService;
    }

    public void updateAgencyTokenUsageForUser(Identity identity){


        // Find email for civil servant, its the domain
        String domain = identity.getEmail();
        // Find org or org code for civil servant, its the code
        String code = "TODO";

        // Find out if the civil servant is a token person, if so return the agencytoken
        // domain and email???
        // domain and code (email and org code)
        ResponseEntity<AgencyTokenResponseDTO> getAgencyTokenResponse = csrsService.getAgencyTokenForCivilServant(domain, code);
        String tokenCode = getAgencyTokenResponse.getBody().getToken();

        // Update the agencyToken useage for this civil servant
        // String code, String domain, String token
        ResponseEntity csrsUpdateAgencyTokenQuotaResponse = csrsService.updateAgencyTokenForCivilServant(code, domain, tokenCode);

    }

}

package uk.gov.cshr.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;

@Transactional
@Service
public class AgencyTokenService {

    private final CSRSService csrsService;

    public AgencyTokenService(CSRSService csrsService) {
        this.csrsService = csrsService;
    }

    public boolean updateAgencyTokenUsageForUser(Identity identity){

        boolean isSuccessful = false;

        // Find email for civil servant, its the domain
        String domain = identity.getEmail();
        // Find org or org code for civil servant, its the code
        ResponseEntity<String> getOrgCodeResponse = csrsService.getOrganisationCodeForCivilServant(identity.getUid());

        if(getOrgCodeResponse.getStatusCode() == HttpStatus.OK) {
            String code = getOrgCodeResponse.getBody();
            // Find out if the civil servant is a token person, if so return the agencytoken
            // domain and email???
            // domain and code (email and org code)
            ResponseEntity<AgencyTokenResponseDTO> getAgencyTokenResponse = csrsService.getAgencyTokenForCivilServant(domain, code);

            if(getAgencyTokenResponse.getStatusCode() == HttpStatus.OK) {
                String tokenCode = getAgencyTokenResponse.getBody().getToken();

                // Update the agencyToken usage for this civil servant
                // String code, String domain, String token
                ResponseEntity csrsUpdateAgencyTokenQuotaResponse = csrsService.updateAgencyTokenForCivilServant(code, domain, tokenCode, true);

                if(csrsUpdateAgencyTokenQuotaResponse.getStatusCode() == HttpStatus.OK) {
                    isSuccessful = true;
                }
            }
        }

        return isSuccessful;
    }

}

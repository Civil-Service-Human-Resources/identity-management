package uk.gov.cshr.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;

@Service
public class AgencyTokenService {

    private final CSRSService csrsService;

    public AgencyTokenService(CSRSService csrsService) {
        this.csrsService = csrsService;
    }

    // TODO - WHERE SHOULD THIS METHOD BE CALLED? - IS IT WHEN A USER IS DELETED OR AT THE CHANGING TO INACTIVE STAGE
    /**
     * Free up the AgencyToken quota as there is one less user.
     * If user is an AgencyToken user then increase spaces available for their AgencyToken by 1.
     * If user is not an AgencyToken user then just return true.
     * @param identity      identity object of the user.  b
     * @param isRemoveUser  indicates whether we should free up the AgencyToken quota by 1 or use up the AgencyToken quota by 1
     * @return boolean      true if user is not a AgencyToken user.
     *                      true if user is a AgencyToken user and method has successfully completed.
     *                      false if if user is a AgencyToken user and method has NOT successfully completed.
     */
    @Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public boolean updateAgencyTokenQuotaForUser(Identity identity, boolean isRemoveUser){
        /*
         * Ensure any call to this method is part of a transaction.
         * Isolation level of Serializable ensures that dirty reads, non-repeatable reads and phantom reads do not occur.
         * Ensure that all ANY exception causes a rollback (not just unchecked).
         */
        boolean isSuccessful = false;

        // Find email for civil servant, its the domain
        String domain = identity.getEmail();
        // Find org code for civil servant, its the code
        ResponseEntity<String> getOrgCodeResponse = csrsService.getOrganisationCodeForCivilServant(identity.getUid());

        if(getOrgCodeResponse.getStatusCode() == HttpStatus.OK) {
            String code = getOrgCodeResponse.getBody();
            // Find out if the civil servant is a token person, if so return the agencytoken
            // domain and code (email and org code)
            ResponseEntity<AgencyTokenResponseDTO> getAgencyTokenResponse = csrsService.getAgencyTokenForCivilServant(domain, code);

            if(getAgencyTokenResponse.getStatusCode() == HttpStatus.OK) {
                String tokenCode = getAgencyTokenResponse.getBody().getToken();

                // Update the agencyToken usage for this civil servant
                ResponseEntity csrsUpdateAgencyTokenQuotaResponse = csrsService.updateAgencyTokenForCivilServant(code, domain, tokenCode, isRemoveUser);

                if(csrsUpdateAgencyTokenQuotaResponse.getStatusCode() == HttpStatus.OK) {
                    isSuccessful = true;
                }
            }
        }

        return isSuccessful;
    }

}

package uk.gov.cshr.service.csrs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.service.RequestEntityFactory;

@Service
public class CsrsService {

    private final HttpClient httpClient;
    private final RequestEntityFactory requestEntityFactory;
    private final String csrsDeleteUrl;
    private final String csrsGetCivilServantUrl;
    private final String agencyTokensUrl;
    private final String updateOtherOrgUnitsUrl;

    public CsrsService(HttpClient httpClient, RequestEntityFactory requestEntityFactory,
                       @Value("${csrs.deleteUrl}") String csrsDeleteUrl,
                       @Value("${csrs.getCivilServant}") String csrsGetCivilServantUrl,
                       @Value("${csrs.getAgencyToken}") String agencyTokensUrl,
                       @Value("${csrs.updateOtherOrgUnitsUrl}") String updateOtherOrgUnitsUrl) {
        this.httpClient = httpClient;
        this.requestEntityFactory = requestEntityFactory;
        this.csrsDeleteUrl = csrsDeleteUrl;
        this.csrsGetCivilServantUrl = csrsGetCivilServantUrl;
        this.agencyTokensUrl = agencyTokensUrl;
        this.updateOtherOrgUnitsUrl = updateOtherOrgUnitsUrl;
    }

    public AgencyTokenDto getAgencyToken(String uid) {
        String url = String.format("%s?uid=%s", agencyTokensUrl, uid);
        RequestEntity<Void> requestEntity = requestEntityFactory.createGetRequest(url);
        try {
            return httpClient.sendRequestNoRetries(requestEntity, AgencyTokenDto.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public ResponseEntity<Void> deleteCivilServant(String uid) {
        RequestEntity<Void> requestEntity = requestEntityFactory.createDeleteRequest(String.format(csrsDeleteUrl, uid));
        return httpClient.sendRequest(requestEntity, Void.class);
    }

    public CivilServantDto getCivilServant(String uid) {
        RequestEntity<Void> requestEntity = requestEntityFactory.createGetRequest(String.format(csrsGetCivilServantUrl, uid));
        try {
            return httpClient.sendRequestNoRetries(requestEntity, CivilServantDto.class).getBody();
        } catch (RestClientResponseException e) {
            if (e.getRawStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public String updateOtherOrganisationalUnits(String civilServantId, UpdateOtherOrgUnitsParams updateOtherOrgUnitsParams) {
        RequestEntity<UpdateOtherOrgUnitsParams> requestEntity = requestEntityFactory.createPatchRequest(
                String.format(updateOtherOrgUnitsUrl, civilServantId), updateOtherOrgUnitsParams);
        return httpClient.sendPatchRequestNoRetries(requestEntity, String.class).getBody();
    }
}

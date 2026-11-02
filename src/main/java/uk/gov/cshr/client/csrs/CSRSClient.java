package uk.gov.cshr.client.csrs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import uk.gov.cshr.client.IHttpClient;
import uk.gov.cshr.client.exception.NotFoundException;
import uk.gov.cshr.service.csrs.AgencyTokenDto;
import uk.gov.cshr.service.csrs.CivilServantDto;
import uk.gov.cshr.service.csrs.UpdateOtherOrgUnitsParams;

@Component
@Slf4j
public class CSRSClient implements ICSRSClient {

    private final IHttpClient httpClient;
    private final String csrsDeleteUrl;
    private final String csrsGetCivilServantUrl;
    private final String agencyTokensUrl;
    private final String updateOtherOrgUnitsUrl;

    public CSRSClient(@Qualifier("csrsHttpClient") IHttpClient httpClient,
                      @Value("${csrs.deleteUrl}") String csrsDeleteUrl,
                      @Value("${csrs.getCivilServant}") String csrsGetCivilServantUrl,
                      @Value("${csrs.getAgencyToken}") String agencyTokensUrl,
                      @Value("${csrs.updateOtherOrgUnitsUrl}") String updateOtherOrgUnitsUrl) {
        this.httpClient = httpClient;
        this.csrsDeleteUrl = csrsDeleteUrl;
        this.csrsGetCivilServantUrl = csrsGetCivilServantUrl;
        this.agencyTokensUrl = agencyTokensUrl;
        this.updateOtherOrgUnitsUrl = updateOtherOrgUnitsUrl;
    }

    public AgencyTokenDto getAgencyToken(String uid) {
        String url = String.format("%s?uid=%s", agencyTokensUrl, uid);
        RequestEntity<Void> requestEntity = RequestEntity.get(url).build();
        try {
            return httpClient.executeRequest(requestEntity, AgencyTokenDto.class);
        } catch (NotFoundException e) {
            return null;
        }
    }

    public void deleteCivilServant(String uid) {
        RequestEntity<Void> requestEntity = RequestEntity.delete(String.format(csrsDeleteUrl, uid)).build();
        httpClient.executeRequest(requestEntity, Void.class);
    }

    public CivilServantDto getCivilServant(String uid) {
        RequestEntity<Void> requestEntity = RequestEntity.get(String.format(csrsGetCivilServantUrl, uid)).build();
        try {
            return httpClient.executeRequest(requestEntity, CivilServantDto.class);
        } catch (NotFoundException e) {
            return null;
        }
    }

    public String updateOtherOrganisationalUnits(String civilServantId, UpdateOtherOrgUnitsParams updateOtherOrgUnitsParams) {
        RequestEntity<UpdateOtherOrgUnitsParams> requestEntity = RequestEntity.patch(String.format(updateOtherOrgUnitsUrl, civilServantId))
                .body(updateOtherOrgUnitsParams);
        return httpClient.executeRequest(requestEntity, String.class);
    }

}

package uk.gov.cshr.service.csrs;

import org.springframework.stereotype.Service;
import uk.gov.cshr.client.csrs.ICSRSClient;

@Service
public class CsrsService {

    private final ICSRSClient client;

    public CsrsService(ICSRSClient client) {
        this.client = client;
    }

    public AgencyTokenDto getAgencyToken(String uid) {
        return client.getAgencyToken(uid);
    }

    public void deleteCivilServant(String uid) {
        client.deleteCivilServant(uid);
    }

    public CivilServantDto getCivilServant(String uid) {
        return client.getCivilServant(uid);
    }

    public String updateOtherOrganisationalUnits(String civilServantId, UpdateOtherOrgUnitsParams updateOtherOrgUnitsParams) {
        return client.updateOtherOrganisationalUnits(civilServantId, updateOtherOrgUnitsParams);
    }
}

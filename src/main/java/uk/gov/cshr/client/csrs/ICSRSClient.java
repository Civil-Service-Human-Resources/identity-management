package uk.gov.cshr.client.csrs;

import uk.gov.cshr.service.csrs.AgencyTokenDto;
import uk.gov.cshr.service.csrs.CivilServantDto;
import uk.gov.cshr.service.csrs.UpdateOtherOrgUnitsParams;

public interface ICSRSClient {
    AgencyTokenDto getAgencyToken(String uid);

    void deleteCivilServant(String uid);

    CivilServantDto getCivilServant(String uid);

    String updateOtherOrganisationalUnits(String civilServantId, UpdateOtherOrgUnitsParams updateOtherOrgUnitsParams);

}

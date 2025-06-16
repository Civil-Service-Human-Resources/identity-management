package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateOtherOrgUnitsParams {
    private List<String> otherOrganisationalUnits;
}

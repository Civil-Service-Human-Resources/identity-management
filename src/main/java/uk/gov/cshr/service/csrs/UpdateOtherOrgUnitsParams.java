package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class UpdateOtherOrgUnitsParams implements Serializable {
    private List<String> otherOrganisationalUnits;
}

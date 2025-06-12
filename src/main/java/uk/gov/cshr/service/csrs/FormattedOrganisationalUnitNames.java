package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormattedOrganisationalUnitNames implements Serializable {
    private List<FormattedOrganisationalUnitName> formattedOrganisationalUnitNames;
}

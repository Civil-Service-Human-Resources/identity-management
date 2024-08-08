package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganisationalUnit {
    private Long id;
    private String name;
    private String code;
    private String abbreviation;

    public String toDisplayString() {
        return String.format("%s (%s) (code: %s)", name, abbreviation, code);
    }
}

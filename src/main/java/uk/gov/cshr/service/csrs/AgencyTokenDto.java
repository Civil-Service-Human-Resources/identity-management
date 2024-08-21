package uk.gov.cshr.service.csrs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgencyTokenDto {
    private String token;
    private String uid;
    private Integer capacity;
    private List<Domain> agencyDomains;
}

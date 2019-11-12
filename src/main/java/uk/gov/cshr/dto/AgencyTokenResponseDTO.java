package uk.gov.cshr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AgencyTokenResponseDTO {

    private Long id;
    private String token;
    private int capacity;
    private int capacityUsed;

}

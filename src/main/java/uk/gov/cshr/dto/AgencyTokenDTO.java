package uk.gov.cshr.dto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AgencyTokenDTO {

    private Long id;
    private String token;
    private int capacity;
    private int capacityUsed;

}

package uk.gov.cshr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateSpacesForAgencyTokenRequestDTO {

    private String domain;
    private String token;
    private String code;
    private boolean removeUser;
}
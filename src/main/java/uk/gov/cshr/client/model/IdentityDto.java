package uk.gov.cshr.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IdentityDto {

    private String uid;
    private List<String> roles;

}

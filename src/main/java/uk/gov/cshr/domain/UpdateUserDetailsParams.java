package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateUserDetailsParams {
    private List<String> uids;
}

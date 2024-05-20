package uk.gov.cshr.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RemoveUserDetailsParams {
    private List<String> uids;
}

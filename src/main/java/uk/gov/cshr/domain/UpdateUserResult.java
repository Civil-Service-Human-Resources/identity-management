package uk.gov.cshr.domain;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class UpdateUserResult {

    private Integer affectedRows;

}

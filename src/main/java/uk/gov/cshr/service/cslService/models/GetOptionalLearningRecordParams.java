package uk.gov.cshr.service.cslService.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetOptionalLearningRecordParams {
    int page = 0;
    int size = 20;
    String q = null;
}

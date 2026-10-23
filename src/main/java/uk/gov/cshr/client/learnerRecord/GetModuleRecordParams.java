package uk.gov.cshr.client.learnerRecord;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class GetModuleRecordParams {

    private final Set<String> userIds;
    private final Set<String> moduleIds;

}

package uk.gov.cshr.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BulkSendEmailResponse {

    private List<String> successfulEmailRefs;
    private List<FailedResource<MessageDto>> failedEmails;

}

package uk.gov.cshr.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkSendEmail {

    private Collection<MessageDto> emails;

}


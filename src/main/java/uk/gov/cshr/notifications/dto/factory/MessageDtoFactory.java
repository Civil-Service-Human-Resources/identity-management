package uk.gov.cshr.notifications.dto.factory;

import org.springframework.stereotype.Component;
import uk.gov.cshr.notifications.dto.MessageDto;

import java.util.Map;

@Component
public class MessageDtoFactory {
    public MessageDto create(String recipient, String templateName, Map<String, String> personalisation) {
        MessageDto messageDto = new MessageDto();
        messageDto.setRecipient(recipient);
        messageDto.setName(templateName);
        messageDto.setPersonalisation(personalisation);

        return messageDto;
    }
}

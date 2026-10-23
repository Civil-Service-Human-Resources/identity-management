package uk.gov.cshr.notifications.service;


import org.junit.jupiter.api.Test;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.dto.factory.MessageDtoFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class MessageServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String CODE = "code";

    private MessageDtoFactory messageDtoFactory = mock(MessageDtoFactory.class);

    private String suspensionMessageTemplateId = "suspensionMessageTemplateId";

    private String deletionMessageTemplateId = "deletionMessageTemplateId";

    private String deletedMessageTemplateId = "deletedMessageTemplateId";

    private String reactivationTemplateId = "reactivationTemplateId";

    private String resetUrl = "resetUrl";
    private String reactivationUrl = "www.example.com/reactivate/";

    private MessageService messageService = new MessageService(
            suspensionMessageTemplateId,
            deletionMessageTemplateId,
            deletedMessageTemplateId,
            reactivationTemplateId,
            resetUrl,
            reactivationUrl,
            messageDtoFactory);

    @Test
    public void createReactivationMessage() {
        Reactivation reactivation = new Reactivation();
        reactivation.setCode(CODE);

        Map<String, String> map = new HashMap<>();
        map.put("learnerName", EMAIL);
        map.put("reactivationUrl", String.format(reactivationUrl, CODE));

        Identity identity = new Identity("uid",
                EMAIL,
                "password",
                true,
                false,
                null,
                Instant.now(),
                false, null);

        messageService.createReactivationMessage(identity, reactivation);

        verify(messageDtoFactory, times(1)).create(EMAIL, reactivationTemplateId, map);
    }
}

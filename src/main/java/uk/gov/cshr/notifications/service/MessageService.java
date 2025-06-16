package uk.gov.cshr.notifications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.dto.factory.MessageDtoFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final MessageDtoFactory messageDtoFactory;

    private final String suspensionMessageTemplateName;

    private final String deletionMessageTemplateName;

    private final String deletedMessageTemplateName;

    private final String reactivationTemplateName;

    private final String resetUrl;

    private final String reactivationUrl;

    public MessageService(@Value("${govNotify.templateName.accountSuspension}") String suspensionMessageTemplateName,
                          @Value("${govNotify.templateName.accountDeletion}") String deletionMessageTemplateName,
                          @Value("${govNotify.templateName.accountDeleted}") String deletedMessageTemplateName,
                          @Value("${govNotify.templateName.reactivation}") String reactivationTemplateName,
                          @Value("${security.oauth2.client.reset-uri}") String resetUrl,
                          @Value("${security.oauth2.client.reactivation-url}") String reactivationUrl,
                          MessageDtoFactory messageDtoFactory
    ) {
        this.messageDtoFactory = messageDtoFactory;
        this.suspensionMessageTemplateName = suspensionMessageTemplateName;
        this.deletionMessageTemplateName = deletionMessageTemplateName;
        this.deletedMessageTemplateName = deletedMessageTemplateName;
        this.reactivationTemplateName = reactivationTemplateName;
        this.resetUrl = resetUrl;
        this.reactivationUrl = reactivationUrl;
    }

    public MessageDto createSuspensionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("resetUrl", resetUrl);

        return messageDtoFactory.create(identity.getEmail(), suspensionMessageTemplateName, map);
    }

    public MessageDto createDeletionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("resetUrl", resetUrl);

        return messageDtoFactory.create(identity.getEmail(), deletionMessageTemplateName, map);
    }

    public MessageDto createDeletedMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());

        return messageDtoFactory.create(identity.getEmail(), deletedMessageTemplateName, map);
    }

    public MessageDto createReactivationMessage(Identity identity, Reactivation reactivation) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("reactivationUrl", String.format(reactivationUrl, reactivation.getCode()));

        return messageDtoFactory.create(identity.getEmail(), reactivationTemplateName, map);
    }
}

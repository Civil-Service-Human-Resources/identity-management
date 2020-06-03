package uk.gov.cshr.notifications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.notifications.dto.MessageDto;
import uk.gov.cshr.notifications.dto.factory.MessageDtoFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {

    private final MessageDtoFactory messageDtoFactory;

    private final String suspensionMessageTemplateId;

    private final String deletionMessageTemplateId;

    private final String invitationMessageTemplateId;

    private final String deletedMessageTemplateId;

    private final String reactivationTemplateId;

    private final String resetUrl;

    private final String reactivationUrl;

    @Value("${invite.url}")
    private String signupUrlFormat;

    public MessageService(@Value("${govNotify.template.accountSuspension}") String suspensionMessageTemplateId,
                          @Value("${govNotify.template.accountDeletion}") String deletionMessageTemplateId,
                          @Value("${govNotify.template.invite}") String invitationMessageTemplateId,
                          @Value("${govNotify.template.accountDeleted}") String deletedMessageTemplateId,
                          @Value("${govNotify.template.reactivationTemplateId}") String reactivationTemplateId,
                          @Value("${security.oauth2.client.reset-uri}") String resetUrl,
                          @Value("${security.oauth2.client.reactivation-url}") String reactivationUrl,
                          MessageDtoFactory messageDtoFactory
    ) {
        this.messageDtoFactory = messageDtoFactory;
        this.suspensionMessageTemplateId = suspensionMessageTemplateId;
        this.deletionMessageTemplateId = deletionMessageTemplateId;
        this.invitationMessageTemplateId = invitationMessageTemplateId;
        this.deletedMessageTemplateId = deletedMessageTemplateId;
        this.reactivationTemplateId = reactivationTemplateId;
        this.resetUrl = resetUrl;
        this.reactivationUrl = reactivationUrl;
    }

    public MessageDto createInvitationnMessage(Invite invite) {
        String activationUrl = String.format(signupUrlFormat, invite.getCode());

        Map<String, String> map = new HashMap<>();

        map.put("email", invite.getForEmail());
        map.put("activationUrl", activationUrl);

        return messageDtoFactory.create(invite.getForEmail(), invitationMessageTemplateId, map);
    }

    public MessageDto createSuspensionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("resetUrl", resetUrl);

        return messageDtoFactory.create(identity.getEmail(), suspensionMessageTemplateId, map);
    }

    public MessageDto createDeletionMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("resetUrl", resetUrl);

        return messageDtoFactory.create(identity.getEmail(), deletionMessageTemplateId, map);
    }

    public MessageDto createDeletedMessage(Identity identity) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());

        return messageDtoFactory.create(identity.getEmail(), deletedMessageTemplateId, map);
    }

    public MessageDto createReactivationMessage(Identity identity, Reactivation reactivation) {
        Map<String, String> map = new HashMap<>();

        map.put("learnerName", identity.getEmail());
        map.put("reactivationUrl", String.format(reactivationUrl, reactivation.getCode()));

        return messageDtoFactory.create(identity.getEmail(), reactivationTemplateId, map);
    }
}

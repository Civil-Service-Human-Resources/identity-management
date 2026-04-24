package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import uk.gov.cshr.config.CustomOAuth2Authentication;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;

import java.util.Optional;

@Slf4j
public abstract class BaseIdentityController {

    protected static final String IDENTITY_ATTRIBUTE = "identity";

    protected static final String REDIRECT_IDENTITIES_LIST = "redirect:/identities";
    protected static final String REDIRECT_IDENTITY_UPDATE = "redirect:/identities/update/%s";
    protected static final String REDIRECT_IDENTITY_ROLES = "redirect:/identities/update/%s/roles";
    protected static final String REDIRECT_IDENTITY_OTHER_ORGANISATION_ACCESS = "redirect:/identities/update/%s/other-organisation-access";
    protected static final String REDIRECT_IDENTITIES_REACTIVATE = "redirect:/identities/reactivate/";

    protected final IdentityRepository identityRepository;

    protected BaseIdentityController(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @ModelAttribute
    public void addTab(Model model) {
        model.addAttribute("tab", "identities");
    }

    protected Identity getIdentity(Model model, String uid, CustomOAuth2Authentication auth, String attribute) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        if (!optionalIdentity.isPresent()) {
            log.info("No identity found for uid {}", uid);
            return null;
        }
        Identity identity = optionalIdentity.get();
        model.addAttribute(IDENTITY_ATTRIBUTE, identity);
        log.info("{} viewing {} for {}", auth.getUserEmail(), attribute, identity.getEmail());
        return identity;
    }

}

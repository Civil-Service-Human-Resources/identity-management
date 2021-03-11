package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.utils.ApplicationConstants;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@PreAuthorize("hasPermission(returnObject, 'read')")
public class IdentityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityController.class);

    public static final String REDIRECT_IDENTITIES_LIST = "redirect:/identities";
    private static final String IDENTITY_ATTRIBUTE = "identity";
    private static final String UID_ATTRIBUTE = "uid";
    private static final String REDIRECT_IDENTITIES_REACTIVATE = "redirect:/identities/reactivate/";
    private static final String IDENTITY_REACTIVATE_TEMPLATE = "identity/reactivate";
    private IdentityRepository identityRepository;

    private RoleRepository roleRepository;

    private IdentityService identityService;

    private ReactivationService reactivationService;

    public IdentityController(IdentityRepository identityRepository,
                              RoleRepository roleRepository,
                              IdentityService identityService,
                              ReactivationService reactivationService) {
        this.identityRepository = identityRepository;
        this.roleRepository = roleRepository;
        this.identityService = identityService;
        this.reactivationService = reactivationService;
    }

    @GetMapping("/identities")
    public String identities(Model model, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        LOGGER.debug("Listing all identities");

        Page<Identity> pages = query == null || query.isEmpty() ? identityRepository.findAll(pageable) : identityRepository.findAllByEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "identity/list";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable(UID_ATTRIBUTE) String uid,
                                 Principal principal) {

        LOGGER.info("{} editing identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute(IDENTITY_ATTRIBUTE, identity);
            model.addAttribute("roles", roles);
            return "identity/edit";
        }

        LOGGER.info("No identity found for uid {}", uid);
        return REDIRECT_IDENTITIES_LIST;
    }

    @PostMapping("/identities/active")
    public String updateActive(@RequestParam(UID_ATTRIBUTE) String uid,
                               RedirectAttributes redirectAttributes) {
        try {
            Identity identity = identityService.getIdentity(uid);

            if (identity.isActive()) {
                identity.setActive(false);
                identity.setAgencyTokenUid(null);
                identityRepository.save(identity);

                redirectAttributes.addFlashAttribute(ApplicationConstants.SUCCESS_ATTRIBUTE, String.format("%s deactivated successfully", identity.getEmail()));

                return REDIRECT_IDENTITIES_LIST;
            } else {
                return REDIRECT_IDENTITIES_REACTIVATE + uid;
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }
    }

    @GetMapping("/identities/reactivate/{uid}")
    public String reactivateUser(@PathVariable(UID_ATTRIBUTE) String uid,
                                 Model model) {
        Identity identity = identityService.getIdentity(uid);

        model.addAttribute(IDENTITY_ATTRIBUTE, identity);
        model.addAttribute(UID_ATTRIBUTE, uid);

        return IDENTITY_REACTIVATE_TEMPLATE;
    }

    @PostMapping("/identities/reactivate")
    public String reactivateUser(@RequestParam("uid") String uid,
                                 RedirectAttributes redirectAttributes) {
        try {
            Identity identity = identityService.getIdentity(uid);

            if (!identity.isActive()) {
                Reactivation reactivationRequest = reactivationService.createReactivationRequest(identity.getEmail());
                reactivationService.sendReactivationEmail(identity, reactivationRequest);
                redirectAttributes.addFlashAttribute(ApplicationConstants.SUCCESS_ATTRIBUTE, String.format("Reactivation email verification sent to %s", identity.getEmail()));
                return REDIRECT_IDENTITIES_LIST;
            } else {
                redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.IDENTITY_ALREADY_ACTIVE_ERROR);
                return REDIRECT_IDENTITIES_LIST;
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }
    }

    @PostMapping("/identities/locked")
    public String updatedLocked(@RequestParam(UID_ATTRIBUTE) String uid,
                                RedirectAttributes redirectAttributes) {
        try {
            identityService.updateLocked(uid);

            return REDIRECT_IDENTITIES_LIST;
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }
    }


    @PostMapping("/identities/update")
    public String identityUpdate(@RequestParam(value = "roleId", required = false) ArrayList<String> roleId,
                                 @RequestParam(UID_ATTRIBUTE) String uid,
                                 RedirectAttributes redirectAttributes) {

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent() && roleId != null) {
            Identity identity = optionalIdentity.get();

            Set<Role> roleSet = new HashSet<>();
            for (String id : roleId) {
                Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));
                if (optionalRole.isPresent()) {
                    roleSet.add(optionalRole.get());
                } else {
                    redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SYSTEM_ERROR);
                    return REDIRECT_IDENTITIES_LIST;
                }
            }
            identity.setRoles(roleSet);
            identityRepository.save(identity);
            identityService.clearUserTokens(identity);
        } else {
            redirectAttributes.addFlashAttribute(ApplicationConstants.STATUS_ATTRIBUTE, ApplicationConstants.SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }

        return REDIRECT_IDENTITIES_LIST;
    }

    @GetMapping("/identities/delete/{uid}")
    @PreAuthorize("hasPermission(returnObject, 'delete')")
    public String getIdentityDelete(Model model, @PathVariable(UID_ATTRIBUTE) String uid, Principal principal) {
        LOGGER.info("{} deleting identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute(IDENTITY_ATTRIBUTE, identity);
            return "identity/delete";
        }

        LOGGER.info("No identity found for uid {}", uid);
        return REDIRECT_IDENTITIES_LIST;
    }

    @Transactional
    @PostMapping("/identities/delete")
    @PreAuthorize("hasPermission(returnObject, 'delete')")
    public String identityDelete(@RequestParam(UID_ATTRIBUTE) String uid) {
        identityService.deleteIdentity(uid);

        return REDIRECT_IDENTITIES_LIST;
    }
}

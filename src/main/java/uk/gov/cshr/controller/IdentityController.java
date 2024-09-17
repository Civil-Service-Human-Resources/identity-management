package uk.gov.cshr.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.cshr.service.CslService;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.csrs.AgencyTokenDto;
import uk.gov.cshr.service.csrs.CSRSService;
import uk.gov.cshr.service.csrs.CivilServantDto;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.*;

import static java.util.Collections.emptyList;
import static uk.gov.cshr.utils.ApplicationConstants.*;

@Slf4j
@Controller
@PreAuthorize("hasPermission(returnObject, 'read')")
@AllArgsConstructor
public class IdentityController {

    public static final String REDIRECT_IDENTITIES_LIST = "redirect:/identities";
    private static final String IDENTITY_ATTRIBUTE = "identity";
    private static final String UID_ATTRIBUTE = "uid";
    private static final String REDIRECT_IDENTITIES_REACTIVATE = "redirect:/identities/reactivate/";
    private static final String IDENTITY_REACTIVATE_TEMPLATE = "identity/reactivate";

    private final IdentityRepository identityRepository;
    private final RoleRepository roleRepository;
    private final IdentityService identityService;
    private final ReactivationService reactivationService;
    private final CSRSService csrsService;
    private final CslService cslService;

    @GetMapping("/identities")
    public String identities(Model model, Pageable pageable,
                             @RequestParam(value = "query", required = false) String query) {
        log.debug("Listing all identities");

        Page<Identity> pages = query == null || query.isEmpty() ? identityRepository.findAll(pageable)
                : identityRepository.findAllByEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "identity/list";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable(UID_ATTRIBUTE) String uid,
                                 Principal principal) {

        log.info("{} editing identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            String tokenUid = identity.getAgencyTokenUid();
            String agencyToken = "None";
            if (tokenUid != null) {
                AgencyTokenDto agencyTokenDto = csrsService.getAgencyToken(identity.getAgencyTokenUid());
                if (agencyTokenDto != null) {
                    agencyToken = agencyTokenDto.getToken();
                }
            }

            identity.setLastReactivation(this.reactivationService.getLatestReactivationForEmail(identity.getEmail()));

            CivilServantDto civilServantDto = csrsService.getCivilServant(uid);
            List<?> requiredCourses = civilServantDto == null ? emptyList()
                    : cslService.getRequiredLearningForUser(uid).getCourses();

            model.addAttribute(IDENTITY_ATTRIBUTE, identity);
            model.addAttribute("requiredCourses", requiredCourses);
            model.addAttribute("roles", roles);
            model.addAttribute("profile", civilServantDto);
            model.addAttribute("token", agencyToken);
            return "identity/edit";
        }

        log.info("No identity found for uid {}", uid);
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

                redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE,
                        String.format("%s deactivated successfully", identity.getEmail()));

                return REDIRECT_IDENTITIES_LIST;
            } else {
                return REDIRECT_IDENTITIES_REACTIVATE + uid;
            }
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
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
            if (identity.isActive()) {
                redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, IDENTITY_ALREADY_ACTIVE_ERROR);
                return REDIRECT_IDENTITIES_LIST;
            }
            Reactivation reactivationRequest = reactivationService.createReactivationRequest(identity.getEmail());
            reactivationService.sendReactivationEmail(identity, reactivationRequest);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE,
                    String.format("Reactivation email verification sent to %s", identity.getEmail()));
            return REDIRECT_IDENTITIES_LIST;
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
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
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, IDENTITY_RESOURCE_NOT_FOUND_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
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
                    redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
                    return REDIRECT_IDENTITIES_LIST;
                }
            }
            identity.setRoles(roleSet);
            identityRepository.save(identity);
        } else {
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }

        return REDIRECT_IDENTITIES_LIST;
    }

    @GetMapping("/identities/delete/{uid}")
    @PreAuthorize("hasPermission(returnObject, 'delete')")
    public String getIdentityDelete(Model model, @PathVariable(UID_ATTRIBUTE) String uid, Principal principal) {
        log.info("{} deleting identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute(IDENTITY_ATTRIBUTE, identity);
            return "identity/delete";
        }

        log.info("No identity found for uid {}", uid);
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

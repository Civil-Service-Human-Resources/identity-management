package uk.gov.cshr.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.config.CustomOAuth2Authentication;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exceptions.ResourceNotFoundException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.CslService;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.csrs.*;
import uk.gov.cshr.service.security.IdentityManagementService;
import uk.gov.cshr.service.security.IdentityService;

import java.util.*;
import java.util.stream.Collectors;

import static com.mysql.cj.util.StringUtils.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.cshr.utils.ApplicationConstants.*;

@Slf4j
@Controller
@PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).READ_IDENTITY)")
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
    private final IdentityManagementService identityManagementService;

    @GetMapping("/identities")
    public String identities(CustomOAuth2Authentication auth, Model model, Pageable pageable,
                             @RequestParam(value = "query", required = false) String query) {
        log.info(format("User %s searching for identities", auth.getUserEmail()) + (!isNullOrEmpty(query) ? format(" with query '%s'", query) : ""));

        Page<Identity> pages = query == null || query.isEmpty() ? identityRepository.findAll(pageable) : identityRepository.findAllByEmailContains(pageable, query);

        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "identity/list";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable(UID_ATTRIBUTE) String uid,
                                 CustomOAuth2Authentication auth) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            log.info("{} viewing identity for uid {}", auth.getUserEmail(), identity.getEmail());
            String tokenUid = identity.getAgencyTokenUid();
            String agencyToken = "None";
            if (tokenUid != null) {
                AgencyTokenDto agencyTokenDto = csrsService.getAgencyToken(identity.getAgencyTokenUid());
                if (agencyTokenDto != null) {
                    agencyToken = agencyTokenDto.getToken();
                }
            }
            identity.setLastReactivation(this.reactivationService.getLatestReactivationForEmail(identity.getEmail()));
            List<?> requiredCourses = emptyList();
            CivilServantDto civilServantDto = csrsService.getCivilServant(uid);
            if (civilServantDto != null) {
                model.addAttribute("civilServantId", civilServantDto.getUserId());

                FormattedOrganisationalUnitNames formattedOrganisationNames = cslService.getFormattedOrganisationNames();
                model.addAttribute("formattedOrganisationNames", formattedOrganisationNames.getFormattedOrganisationalUnitNames());

                List<FormattedOrganisationalUnitName> assignedFormattedOrganisationNames = emptyList();
                if(civilServantDto.getOtherOrganisationalUnits() != null) {
                    Map<Long, FormattedOrganisationalUnitName> formattedOrgNamesMap = formattedOrganisationNames.getFormattedOrganisationalUnitNames()
                            .stream()
                            .collect(toMap(FormattedOrganisationalUnitName::getId, o -> o));
                    Set<OrganisationalUnit> assignedOtherOrganisations = civilServantDto.getOtherOrganisationalUnits();
                    assignedFormattedOrganisationNames = assignedOtherOrganisations
                            .stream()
                            .map(aoo -> formattedOrgNamesMap.get(aoo.getId()))
                            .sorted(Comparator.comparing(FormattedOrganisationalUnitName::getName))
                            .collect(Collectors.toList());
                }
                model.addAttribute("alreadyAssignedOtherOrganisations", assignedFormattedOrganisationNames);

                String alreadyAssignedOtherOrganisationIds = assignedFormattedOrganisationNames
                        .stream()
                        .map(FormattedOrganisationalUnitName::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                model.addAttribute("alreadyAssignedOtherOrganisationIds", alreadyAssignedOtherOrganisationIds);

                if (civilServantDto.isProfileComplete()) {
                    requiredCourses = cslService.getRequiredLearningForUser(uid).getCourses();
                }
            }
            model.addAttribute("requiredCourses", requiredCourses);
            model.addAttribute(IDENTITY_ATTRIBUTE, identity);
            model.addAttribute("roles", roles);
            model.addAttribute("profile", civilServantDto);
            model.addAttribute("token", agencyToken);
            return "identity/edit";
        }

        log.info("No identity found for uid {}", uid);
        return REDIRECT_IDENTITIES_LIST;
    }

    @PostMapping("/identities/active")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_IDENTITY)")
    public String updateActive(CustomOAuth2Authentication auth,
                               @RequestParam(UID_ATTRIBUTE) String uid,
                               RedirectAttributes redirectAttributes) {
        try {
            Identity identity = identityService.getIdentity(uid);
            log.info("{} attempting to deactivate identity {}", auth.getUserEmail(), identity.getEmail());

            if (identity.isActive()) {
                identityManagementService.deactivateIdentity(identity);
                redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, String.format("%s deactivated successfully", identity.getEmail()));
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
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_IDENTITY)")
    public String reactivateUser(@PathVariable(UID_ATTRIBUTE) String uid,
                                 Model model) {
        Identity identity = identityService.getIdentity(uid);

        model.addAttribute(IDENTITY_ATTRIBUTE, identity);
        model.addAttribute(UID_ATTRIBUTE, uid);

        return IDENTITY_REACTIVATE_TEMPLATE;
    }

    @PostMapping("/identities/reactivate")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_IDENTITY)")
    public String reactivateUser(CustomOAuth2Authentication auth,
                                 @RequestParam("uid") String uid,
                                 RedirectAttributes redirectAttributes) {
        try {
            Identity identity = identityService.getIdentity(uid);
            log.info("{} attempting to activate identity {}", auth.getUserEmail(), identity.getEmail());
            if (identity.isActive()) {
                redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, IDENTITY_ALREADY_ACTIVE_ERROR);
                return REDIRECT_IDENTITIES_LIST;
            }
            Reactivation reactivationRequest = reactivationService.createReactivationRequest(identity.getEmail());
            reactivationService.sendReactivationEmail(identity, reactivationRequest);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE,
                    format("Reactivation email verification sent to %s", identity.getEmail()));
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
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_IDENTITY)")
    public String updatedLocked(CustomOAuth2Authentication auth,
                                @RequestParam(UID_ATTRIBUTE) String uid,
                                RedirectAttributes redirectAttributes) {
        log.info("{} attempting to lock identity {}", auth.getUserEmail(), uid);
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

    @PostMapping("/identities/{uid}/update_roles")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_IDENTITY)")
    public String identityUpdateRoles(CustomOAuth2Authentication auth,
                                      @PathVariable(UID_ATTRIBUTE) String uid,
                                      @RequestParam(value = "roleId", required = false) ArrayList<String> roleId,
                                      RedirectAttributes redirectAttributes) {
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent() && roleId != null) {
            Identity identity = optionalIdentity.get();
            log.info("{} attempting to set the following roles on identity {}: {}", auth.getUserEmail(),
                    identity.getEmail(), String.join(", ", roleId));

            Set<Role> roleSet = new HashSet<>();
            for (String id : roleId) {
                Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));
                if (optionalRole.isPresent()) {
                    roleSet.add(optionalRole.get());
                } else {
                    log.error("Role ID {} not found; aborting role update for UID {}", id, uid);
                    redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
                    return REDIRECT_IDENTITIES_LIST;
                }
            }
            identity.setRoles(roleSet);
            identityRepository.save(identity);
            redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, "Roles updated successfully for user " + identity.getEmail());
        } else {
            log.error("Invalid identity or missing roles for UID {}", uid);
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE, SYSTEM_ERROR);
            return REDIRECT_IDENTITIES_LIST;
        }

        return REDIRECT_IDENTITIES_LIST;
    }

    @GetMapping("/identities/delete/{uid}")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).DELETE_IDENTITY)")
    public String getIdentityDelete(Model model, @PathVariable(UID_ATTRIBUTE) String uid) {
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
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).DELETE_IDENTITY)")
    public String identityDelete(@RequestParam(UID_ATTRIBUTE) String uid, CustomOAuth2Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        log.info("{} deleting identity {}", auth.getUserEmail(), uid);
        identityRepository.findFirstByUid(uid)
                .ifPresent(identity -> {
                    String email = identity.getEmail();
                    identityManagementService.deleteIdentity(identity);
                    redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, String.format("%s deleted successfully", email));
                });
        return REDIRECT_IDENTITIES_LIST;
    }

    @Transactional
    @PostMapping("/identities/{uid}/other-organisations/add")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_ORGANISATIONS)")
    public String assignOtherOrganisationalUnits(CustomOAuth2Authentication auth,
                  @PathVariable("uid") String uid,
                  @RequestParam("civilServantId") String civilServantId,
                  @RequestParam(value = "alreadyAssignedOtherOrganisationIds", required = false)
                            ArrayList<String> alreadyAssignedOtherOrganisationIds,
                  @RequestParam(value = "otherOrgIdsToAdd", required = false) ArrayList<String> otherOrgIdsToAdd,
                  RedirectAttributes redirectAttributes) {
        log.info("{} is adding other organisation ids {} for civilServantId {} and identity id {}", auth.getUserEmail(),
                otherOrgIdsToAdd, civilServantId, uid);
        if (otherOrgIdsToAdd != null && !otherOrgIdsToAdd.isEmpty()) {
            log.debug("alreadyAssignedOtherOrganisationIds: {}", alreadyAssignedOtherOrganisationIds);
            List<String> otherOrganisationalUnits = new ArrayList<>();
            if (alreadyAssignedOtherOrganisationIds != null && !alreadyAssignedOtherOrganisationIds.isEmpty()) {
                for (String alreadyAssignedOtherOrganisationId : alreadyAssignedOtherOrganisationIds) {
                    log.debug("Already assigned other organisation id: {}", alreadyAssignedOtherOrganisationId);
                    otherOrganisationalUnits.add("/organisationalUnits/" + alreadyAssignedOtherOrganisationId);
                }
            }
            for (String otherOrgIdToAdd : otherOrgIdsToAdd) {
                log.debug("Other organisation id to add: {}", otherOrgIdToAdd);
                otherOrganisationalUnits.add("/organisationalUnits/" + otherOrgIdToAdd);
            }
            updateOtherOrganisationalUnits(civilServantId, uid, otherOrganisationalUnits, redirectAttributes);
        }
        return REDIRECT_IDENTITIES_LIST;
    }

    @Transactional
    @PostMapping("/identities/{uid}/other-organisations/{otherOrgIdToRemove}/remove")
    @PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_ORGANISATIONS)")
    public String removeOtherOrganisationalUnits(CustomOAuth2Authentication auth,
                  @PathVariable("uid") String uid,
                  @PathVariable("otherOrgIdToRemove") String otherOrgIdToRemove,
                  @RequestParam("civilServantId") String civilServantId,
                  @RequestParam(value = "alreadyAssignedOtherOrganisationIds", required = false)
                          ArrayList<String> alreadyAssignedOtherOrganisationIds,
                  RedirectAttributes redirectAttributes) {
        log.info("{} is removing other organisation id {} for civilServantId {} and identity id {}", auth.getUserEmail(),
                otherOrgIdToRemove, civilServantId, uid);
        log.debug("alreadyAssignedOtherOrganisationIds: {}", alreadyAssignedOtherOrganisationIds);
        List<String> otherOrganisationalUnits = new ArrayList<>();
        if (alreadyAssignedOtherOrganisationIds == null || alreadyAssignedOtherOrganisationIds.isEmpty()) {
            log.error("No previously assigned organisations found while attempting to remove other organisation ID: {}", otherOrgIdToRemove);
            redirectAttributes.addFlashAttribute(STATUS_ATTRIBUTE,
                    "Could not verify currently assigned other organisations. Other organisation is not removed.");
        } else {
            for (String alreadyAssignedOtherOrganisationId : alreadyAssignedOtherOrganisationIds) {
                log.debug("Already assigned other organisation id: {}", alreadyAssignedOtherOrganisationId);
                if (!Objects.equals(alreadyAssignedOtherOrganisationId, otherOrgIdToRemove)) {
                    otherOrganisationalUnits.add("/organisationalUnits/" + alreadyAssignedOtherOrganisationId);
                }
            }
            updateOtherOrganisationalUnits(civilServantId, uid, otherOrganisationalUnits, redirectAttributes);
        }
        return REDIRECT_IDENTITIES_LIST;
    }

    private void updateOtherOrganisationalUnits(String civilServantId, String uid, List<String> otherOrganisationalUnits,
                                 RedirectAttributes redirectAttributes) {
        String email ="";
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            email = identity.getEmail();
        }
        UpdateOtherOrgUnitsParams updateOtherOrgUnitsParams = new UpdateOtherOrgUnitsParams(otherOrganisationalUnits);
        log.debug("Updating other organisational units {} for user {} ({})", updateOtherOrgUnitsParams, email, uid);
        String updateOtherOrgUnitsResult = csrsService.updateOtherOrganisationalUnits(civilServantId, updateOtherOrgUnitsParams);
        log.info("Other organisational units update is successful for user {} ({}), update result is {}", email, uid, updateOtherOrgUnitsResult);
        redirectAttributes.addFlashAttribute(SUCCESS_ATTRIBUTE, String.format("Other organisational units are updated successfully for user %s", email));
    }
}

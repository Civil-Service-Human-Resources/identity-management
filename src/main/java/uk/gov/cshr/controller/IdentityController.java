package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.exceptions.ForbiddenException;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller

public class IdentityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityController.class);

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private IdentityService identityService;

    @GetMapping("/identities")
    public String identities(Model model, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        LOGGER.info("Listing all identities");

        Page<Identity> pages = query == null || query.isEmpty() ? identityRepository.findAll(pageable) : identityRepository.findAllByEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "identity/list";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable("uid") String uid, Principal principal) {

        LOGGER.info("{} editing identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute("identity", identity);
            model.addAttribute("roles", roles);
            return "identity/edit";
        }

        LOGGER.info("No identity found for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);
        return "redirect:/identities";
    }

    @PostMapping("/identities/update")
    public String identityUpdate(@RequestParam(value = "locked", required = false) Boolean locked, @RequestParam(value = "active", required = false) Boolean active, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, @RequestParam("uid") String uid, Principal principal) {

        // get identity to edit
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();

            Set<Role> roleSet = new HashSet<>();
            // create roles from id
            if (roleId != null) {
                for (String id : roleId) {
                    Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));
                    if (optionalRole.isPresent()) {
                        // got role
                        roleSet.add(optionalRole.get());
                    } else {
                        LOGGER.info("{} found no role for id {}", ((OAuth2Authentication) principal).getPrincipal(), id);
                        // do something here , probably go to error page
                        return "redirect:/identities";
                    }
                }
            }
            // afer this give roleset to identity
            identity.setRoles(roleSet);
            // and update  the active property
            if (active != null) {
                identity.setActive(active);
            } else {
                identity.setActive(false);
            }

            if (locked != null) {
                identity.setLocked(locked);
            } else {
                identity.setLocked(false);
            }

            identityRepository.save(identity);

            LOGGER.info("{} updated new role {}", ((OAuth2Authentication) principal).getPrincipal(), identity);
        } else {
            LOGGER.info("{} found no identity for uid {}", ((IdentityDetails) principal).getUsername(), uid);
            // do something here , probably go to error page
        }

        return "redirect:/identities";
    }

    @GetMapping("/identities/delete/{uid}")
    public String getIdentityDelete(Model model, @PathVariable("uid") String uid, Principal principal) {
        LOGGER.info("{} deleting identity for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute("identity", identity);
            return "identity/delete";
        }

        LOGGER.info("No identity found for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);
        return "redirect:/identities";
    }

    @Transactional
    @PostMapping("/identities/delete/{uid}")
    public String identityDelete(@RequestParam("uid") String uid) {
        identityService.deleteIdentity(uid);

        return "redirect:/identities";
    }

    @Transactional
    @GetMapping("/identities/track")
    public String identityTrack() {
        LOGGER.info("Tracking user activity");
        identityService.trackUserActivity();

        return "redirect:/identities";
    }
}




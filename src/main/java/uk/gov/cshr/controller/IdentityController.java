package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
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
    public String identities(Model model) {
        LOGGER.info("Listing all identities");

        Iterable<Identity> identities = identityRepository.findAll();
        model.addAttribute("identities", identities);
        return "identityList";
    }

    @GetMapping("/identities/update/{uid}")
    public String identityUpdate(Model model,
                                 @PathVariable("uid") String uid, Principal principal) {

        LOGGER.info("{} editing identity for uid {}", ((IdentityDetails) principal).getUsername(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute("identity", identity);
            model.addAttribute("roles", roles);
            return "updateIdentity";
        }

        LOGGER.info("No identity found for uid {}", ((IdentityDetails) principal).getUsername(), uid);
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
                        LOGGER.info("{} found no role for id {}", ((IdentityDetails) principal).getUsername(), id);
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

            if(locked != null) {
                identity.setLocked(locked);
            } else {
                identity.setLocked(false);
            }

            identityRepository.save(identity);

            LOGGER.info("{} updated new role {}", ((IdentityDetails) principal).getUsername(), identity);
        } else {
            LOGGER.info("{} found no identity for uid {}", ((IdentityDetails) principal).getUsername(), uid);
            // do something here , probably go to error page
        }

        return "redirect:/identities";
    }

    @GetMapping("/identities/delete/{uid}")
    public String identityDelete(Model model, @PathVariable("uid") String uid, Principal principal) {
        LOGGER.info("{} deleting identity for uid {}", ((IdentityDetails) principal).getUsername(), uid);

        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);
        Iterable<Role> roles = roleRepository.findAll();

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();
            model.addAttribute("identity", identity);
            return "deleteIdentity";
        }

        LOGGER.info("No identity found for uid {}", ((IdentityDetails) principal).getUsername(), uid);
        return "redirect:/identities";
    }

    @Transactional
    @PostMapping("/identities/delete")
    @PreAuthorize("hasAnyAuthority('IDENTITY_DELETE')")
    public String identityDelete(@RequestParam("uid") String uid) {
        identityService.deleteIdentity(uid);

        return "redirect:/identities";
    }
}

package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/invite")
public class InviteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private InviteService inviteService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private RoleRepository roleRepository;


    @GetMapping
    public String invite(Model model, Principal principal) {
        LOGGER.info("{} on Invite screen", ((IdentityDetails) principal).getUsername());

        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("invites", inviteRepository.findAll());

        return "inviteList";
    }

    @PostMapping
    public String invited(@RequestParam(value = "forEmail") String forEmail, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, RedirectAttributes redirectAttributes, Principal principal) {
        LOGGER.info("{} inviting {} ", ((IdentityDetails) principal).getUsername(), forEmail);

        if (inviteRepository.existsByForEmailAndStatus(forEmail, InviteStatus.PENDING)) {
            LOGGER.info("{} has already been invited", forEmail);
            redirectAttributes.addFlashAttribute("status", forEmail + " has already been invited");
            return "redirect:/invite";
        }

        if (identityService.existsByEmail(forEmail)) {
            LOGGER.info("{} is already a user", forEmail);
            redirectAttributes.addFlashAttribute("status", "User already exists with email address " + forEmail);
            return "redirect:/invite";
        }

        Set<Role> roleSet = new HashSet<>();

        if (roleId != null) {
            for (String id : roleId) {
                Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));

                if (optionalRole.isPresent()) {
                    roleSet.add(optionalRole.get());
                } else {
                    LOGGER.info("{} found no role for id {}", ((IdentityDetails) principal).getUsername(), id);
                    return "redirect:/invite";
                }
            }
        }

        inviteService.createNewInviteForEmailAndRoles(forEmail, roleSet, ((IdentityDetails) principal).getIdentity());

        LOGGER.info("{} invited {}", ((IdentityDetails) principal).getUsername(), forEmail);

        redirectAttributes.addFlashAttribute("status", "Invite sent to " + forEmail);
        return "redirect:/invite";
    }

}
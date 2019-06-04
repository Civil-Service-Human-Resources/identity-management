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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.InviteService;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/invites")
@PreAuthorize("hasPermission(returnObject, 'read')")
public class InviteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Autowired
    private InviteService inviteService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private InviteRepository inviteRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/send")
    public String send(Model model) {

        model.addAttribute("roles", roleRepository.findAll());

        return "invite/send";
    }

    @GetMapping
    public String invite(Model model, Principal principal, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        LOGGER.info("{} on Invite screen", ((OAuth2Authentication) principal).getPrincipal());

        Page<Invite> pages = query == null || query.isEmpty() ? inviteRepository.findAllByInviterNotNullAndInvitedAtNotNull(pageable) : inviteRepository.findAllByForEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "invite/list";
    }

    @PostMapping
    public String invited(@RequestParam(value = "forEmail") String forEmail, @RequestParam(value = "roleId", required = false) ArrayList<String> roleId, RedirectAttributes redirectAttributes, Principal principal) {
        forEmail = forEmail.trim().toLowerCase();
        String actorEmail = ((OAuth2Authentication) principal).getPrincipal().toString();
        LOGGER.info("{} inviting {} ", actorEmail, forEmail);

        if (inviteRepository.existsByForEmailAndStatus(forEmail, InviteStatus.PENDING)) {
            LOGGER.info("{} has already been invited", forEmail);
            redirectAttributes.addFlashAttribute("status", forEmail + " has already been invited");
            return "redirect:/invites";
        }

        if (identityService.existsByEmail(forEmail)) {
            LOGGER.info("{} is already a user", forEmail);
            redirectAttributes.addFlashAttribute("status", "User already exists with email address " + forEmail);
            return "redirect:/invites";
        }

        Set<Role> roleSet = new HashSet<>();

        if (roleId != null) {
            for (String id : roleId) {
                Optional<Role> optionalRole = roleRepository.findById(Long.parseLong(id));

                if (optionalRole.isPresent()) {
                    roleSet.add(optionalRole.get());
                } else {
                    LOGGER.info("{} found no role for id {}", actorEmail, id);
                    return "redirect:/invites";
                }
            }
        }

        inviteService.createNewInviteForEmailAndRoles(forEmail, roleSet, identityRepository.findFirstByActiveTrueAndEmailEquals(actorEmail));

        LOGGER.info("{} invited {}", actorEmail, forEmail);

        redirectAttributes.addFlashAttribute("status", "Invite sent to " + forEmail);
        return "redirect:/invites";
    }

}
package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.Pagination;

import java.security.Principal;

@Controller
@Slf4j
@RequestMapping("/invites")
@PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).READ_INVITES)")
public class InviteController {

    private final InviteRepository inviteRepository;

    private final RoleRepository roleRepository;

    public InviteController(InviteRepository inviteRepository, RoleRepository roleRepository) {
        this.inviteRepository = inviteRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/send")
    public String send(Model model) {

        model.addAttribute("roles", roleRepository.findAll());

        return "invite/send";
    }

    @GetMapping
    public String viewInvites(Model model, Principal principal, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        log.info("{} on Invite screen", ((OAuth2Authentication) principal).getPrincipal());

        Page<Invite> pages = query == null || query.isEmpty() ? inviteRepository.findAllByInviterNotNullAndInvitedAtNotNull(pageable) : inviteRepository.findAllByForEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "invite/list";
    }

}

package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.cshr.config.CustomOAuth2User;
import uk.gov.cshr.identity.entity.Invite;
import uk.gov.cshr.identity.repository.InviteRepository;
import uk.gov.cshr.identity.repository.RoleRepository;
import uk.gov.cshr.service.Pagination;

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

    @ModelAttribute
    public void addTab(Model model) {
        model.addAttribute("tab", "invites");
    }

    @GetMapping("/send")
    public String send(Model model) {

        model.addAttribute("roles", roleRepository.findAll());

        return "invite/send";
    }

    @GetMapping
    public String viewInvites(Model model, @AuthenticationPrincipal CustomOAuth2User auth, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        log.info("{} on Invite screen", auth.getEmail());

        Page<Invite> pages = query == null || query.isEmpty() ? inviteRepository.findAllByInviterNotNullAndInvitedAtNotNull(pageable) : inviteRepository.findAllByForEmailContains(pageable, query);
        model.addAttribute("page", pages);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(pages.getNumber(), pages.getTotalPages()));

        return "invite/list";
    }

}

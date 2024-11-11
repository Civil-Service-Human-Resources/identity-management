package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

import java.security.Principal;
import java.util.Optional;


@Controller
@RequestMapping("/roles")
@Slf4j
@PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).MANAGE_ROLES)")
public class RoleController {
    
    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String roles(Model model) {
        log.info("Listing all roles");

        Iterable<Role> roles = roleRepository.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("role", new Role());

        return "role/list";
    }

    @PostMapping("/create")
    public String roleCreate(@ModelAttribute("role") Role role, Principal principal) {

        log.info("{} created new role {}", ((OAuth2Authentication) principal).getPrincipal(), role);

        if (role.getId() == null) {
            roleRepository.save(role);
        }

        return "redirect:/roles";
    }

    @GetMapping("/update/{id}")
    public String roleUpdate(Model model,
                             @PathVariable("id") long id, Principal principal) {
        log.info("{} updating role for id {}", ((OAuth2Authentication) principal).getPrincipal(), id);

        Optional<Role> optionalRole = roleRepository.findById(id);

        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            model.addAttribute("role", role);
            return "role/edit";
        }

        log.info("No role found for id {}", id);
        return "redirect:/roles";
    }


    @PostMapping("/update")
    public String roleUpdate(@ModelAttribute("role") Role role, Principal principal) {
        roleRepository.save(role);

        log.info("{} updated role {}", ((OAuth2Authentication) principal).getPrincipal(), role);

        return "redirect:/roles";
    }

    @GetMapping("/delete/{id}")
    public String roleDelete(Model model,
                             @PathVariable("id") long id, Principal principal) {
        log.info("{} deleting role for id {}", ((OAuth2Authentication) principal).getPrincipal(), id);

        Optional<Role> role = roleRepository.findById(id);

        if (role.isPresent()) {
            model.addAttribute("role", role.get());
            return "role/delete";
        }

        log.info("No role found for id {}", id);
        return "redirect:/roles";
    }

    @PostMapping("/delete")
    public String roleDelete(@ModelAttribute("role") Role role, Principal principal) {
        roleRepository.delete(role);

        log.info("{} deleted role {}", ((OAuth2Authentication) principal).getPrincipal(), role);

        return "redirect:/roles";
    }

}

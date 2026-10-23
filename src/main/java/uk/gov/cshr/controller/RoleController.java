package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.config.CustomOAuth2Authentication;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;

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

    @ModelAttribute
    public void addTab(Model model) {
        model.addAttribute("tab", "roles");
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
    public String roleCreate(@ModelAttribute("role") Role role, CustomOAuth2Authentication auth) {

        log.info("{} created new role {}", auth.getUserEmail(), role);

        if (role.getId() == null) {
            roleRepository.save(role);
        }

        return "redirect:/roles";
    }

    @GetMapping("/update/{id}")
    public String roleUpdate(Model model,
                             @PathVariable("id") long id, CustomOAuth2Authentication auth) {
        log.info("{} updating role for id {}", auth.getUserEmail(), id);

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
    public String roleUpdate(@ModelAttribute("role") Role role, CustomOAuth2Authentication auth) {
        roleRepository.save(role);

        log.info("{} updated role {}", auth.getUserEmail(), role);

        return "redirect:/roles";
    }

    @GetMapping("/delete/{id}")
    public String roleDelete(Model model,
                             @PathVariable("id") long id, CustomOAuth2Authentication auth) {
        log.info("{} deleting role for id {}", auth.getUserEmail(), id);

        Optional<Role> role = roleRepository.findById(id);

        if (role.isPresent()) {
            model.addAttribute("role", role.get());
            return "role/delete";
        }

        log.info("No role found for id {}", id);
        return "redirect:/roles";
    }

    @PostMapping("/delete")
    public String roleDelete(@ModelAttribute("role") Role role, CustomOAuth2Authentication auth) {
        roleRepository.delete(role);

        log.info("{} deleted role {}", auth.getUserEmail(), role);

        return "redirect:/roles";
    }

}

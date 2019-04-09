package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.security.IdentityDetails;

import java.security.Principal;
import java.util.Optional;


@Controller
@RequestMapping("/roles")
public class RoleController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    public String roles(Model model, Principal principal) {
        LOGGER.info("Listing all roles");

        Iterable<Role> roles = roleRepository.findAll();

        model.addAttribute("roles", roles);
        model.addAttribute("role", new Role());

        return "roleList"; // change from 'roles' testrunner produces common infinite loop exception as confuses 'roles' with /roles
    }

    @PostMapping("/create")
    public String roleCreate(@ModelAttribute("role") Role role, Principal principal) {
        LOGGER.info("{} created new role {}", ((IdentityDetails) principal).getUsername(), role);

        if (role.getId() == null) {
            roleRepository.save(role);
        }

        return "redirect:/roles";
    }

    @GetMapping("/update/{id}")
    public String roleUpdate(Model model,
                             @PathVariable("id") long id, Principal principal) {
        LOGGER.info("{} updating role for id {}", ((IdentityDetails) principal).getUsername(), id);

        Optional<Role> optionalRole = roleRepository.findById(id);

        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            model.addAttribute("role", role);
            return "updateRole";
        }

        LOGGER.info("No role found for id {}", id);
        return "redirect:/roles";
    }


    @PostMapping("/update")
    public String roleUpdate(@ModelAttribute("role") Role role, Principal principal) {
        roleRepository.save(role);

        LOGGER.info("{} updated role {}", ((IdentityDetails) principal).getUsername(), role);

        return "redirect:/roles";
    }

    @GetMapping("/delete/{id}")
    public String roleDelete(Model model,
                             @PathVariable("id") long id, Principal principal) {
        LOGGER.info("{} deleting role for id {}", ((IdentityDetails) principal).getUsername(), id);

        Optional<Role> role = roleRepository.findById(id);

        if (role.isPresent()) {
            model.addAttribute("role", role.get());
            return "deleteRole";
        }

        LOGGER.info("No role found for id {}", id);
        return "redirect:/roles";
    }

    @PostMapping("/delete")
    public String roleDelete(@ModelAttribute("role") Role role, Principal principal) {
        roleRepository.delete(role);

        LOGGER.info("{} deleted role {}", ((IdentityDetails) principal).getUsername(), role);

        return "redirect:/roles";
    }

}

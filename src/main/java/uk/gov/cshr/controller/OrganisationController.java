package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.organisation.OrganisationDto;
import uk.gov.cshr.service.organisation.OrganisationService;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/organisation")
@PreAuthorize("hasPermission(returnObject, 'read')")
public class OrganisationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationController.class);

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private IdentityService identityService;

    @GetMapping
    public String getOrganisation(Model model) {

        List<OrganisationDto> organisations = organisationService.getOrganisations();
        model.addAttribute("organisations", organisations);

        return "organisation/add";
    }

    @PostMapping
    public String addOrganisationReportingPermission(@RequestParam(value = "forEmail") String forEmail, @RequestParam(value = "organisationId", required = true) ArrayList<String> organisationId, Model model, Principal principal) {
        forEmail = forEmail.trim().toLowerCase();
        String actorEmail = ((OAuth2Authentication) principal).getPrincipal().toString();
        LOGGER.info("{} inviting {} ", actorEmail, forEmail);
        String uid = identityService.getUIDFromEmail(forEmail);
        if (uid != null) {
            boolean response = organisationService.addOrganisationReportingPermission(uid, organisationId);
            if(response) {
                return "redirect:/organisation";
            } else {
                model.addAttribute("error", "There is some problem at the momennt, try again later");
                return "redirect:/error";
            }
        } else {
            model.addAttribute("error", "User already exists with email address " + forEmail);
            return "redirect:/error";
        }
    }

}
package uk.gov.cshr.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.dto.OrganisationDto;
import uk.gov.cshr.service.organisation.ReportingPermissionService;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/reportingpermission")
@PreAuthorize("hasPermission(returnObject, 'read')")
public class ReportingPermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingPermissionController.class);

    @Autowired
    private ReportingPermissionService reportingPermissionService;

    @Autowired
    private IdentityService identityService;

    @GetMapping
    public String listUserWithReportingPermission(Model model, Pageable pageable, @RequestParam(value = "query", required = false) String query) {
        LOGGER.info("Listing all users with reporting permission");

        List<String> listCivilServantUid = reportingPermissionService.getCivilServantUIDsWithReportingPermission();
        Page<Identity> listUser = identityService.getAllIdentityFromUid(pageable, listCivilServantUid);

        model.addAttribute("page", listUser);
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("pagination", Pagination.generateList(listUser.getNumber(), listUser.getTotalPages()));

        return "reportingpermission/list";
    }


    @GetMapping("/add")
    public String getOrganisation(Model model) {

        List<OrganisationDto> organisations = reportingPermissionService.getOrganisations();
        model.addAttribute("organisations", organisations);

        return "reportingpermission/add";
    }

    @PostMapping
    public String addOrganisationReportingPermission(@RequestParam(value = "forEmail") String forEmail, @RequestParam(value = "organisationId", required = true) List<String> organisationId, Model model, Principal principal) {
        forEmail = forEmail.trim().toLowerCase();
        String actorEmail = ((OAuth2Authentication) principal).getPrincipal().toString();
        LOGGER.info("{} inviting {} ", actorEmail, forEmail);
        String uid = identityService.getUIDFromEmail(forEmail);
        if (uid != null) {
            boolean response = reportingPermissionService.addOrganisationReportingPermission(uid, organisationId);
            if(response) {
                return "redirect:/reportingpermission";
            } else {
                model.addAttribute("error", "There is some problem at the moment, try again later");
                return "redirect:/error";
            }
        } else {
            model.addAttribute("error", "User already exists with email address " + forEmail);
            return "redirect:/error";
        }
    }

}
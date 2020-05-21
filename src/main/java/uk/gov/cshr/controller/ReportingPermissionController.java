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
import uk.gov.cshr.domain.Role;
import uk.gov.cshr.dto.ReportingPermissionDto;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.dto.OrganisationDto;
import uk.gov.cshr.service.organisation.ReportingPermissionService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/reportingpermission")
@PreAuthorize("hasPermission(returnObject, 'read')")
public class ReportingPermissionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingPermissionController.class);

    @Autowired
    private ReportingPermissionService reportingPermissionService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private IdentityRepository identityRepository;

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

    @DeleteMapping("/{uid}")
    @PreAuthorize("hasAnyAuthority('IDENTITY_DELETE')")
    public ResponseEntity deleteLearner(@PathVariable String uid) {
        LOGGER.info("Deleting reporting permission for civil servant with uid {}", uid);
//
//        learnerService.deleteLearnerByUid(uid);
//
//        LOGGER.info("Learner deleted with uid {}", uid);
//
       return ResponseEntity.noContent().build();
    }

    @GetMapping("/update/{uid}")
    public String showUpdateReportingPermission(Model model,
                                 @PathVariable("uid") String uid, Principal principal) {
        LOGGER.info("{} editing reporting permission for uid {}", ((OAuth2Authentication) principal).getPrincipal(), uid);

        List<OrganisationDto> organisations = reportingPermissionService.getOrganisations();
        List<String> listCivilServantReportingPermission  = reportingPermissionService.getCivilServantReportingPermission(uid);
        Optional<Identity> optionalIdentity = identityRepository.findFirstByUid(uid);

        model.addAttribute("organisations", organisations);
        model.addAttribute("permissions", listCivilServantReportingPermission);
        model.addAttribute("identity", optionalIdentity.isPresent() ? optionalIdentity.get() : null);
        return "reportingpermission/edit";
    }

    @PostMapping("/update")
    public String updateReportingPermission(@RequestParam(value = "organisationId", required = true) List<String> listOrganisationId, @RequestParam("uid") String uid, Model model, Principal principal) {

        boolean response = reportingPermissionService.updateOrganisationReportingPermission(uid, listOrganisationId);
        if(response) {
            return "redirect:/reportingpermission";
        } else {
            model.addAttribute("error", "There is some problem at the moment, try again later");
            return "redirect:/error";
        }
    }

}
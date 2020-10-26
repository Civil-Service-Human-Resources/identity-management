package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.repository.RoleRepository;
import uk.gov.cshr.service.ReactivationService;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Slf4j
@Controller
@PreAuthorize("hasPermission(returnObject, 'read')")
public class UserController {

    private IdentityRepository identityRepository;

    private RoleRepository roleRepository;

    private IdentityService identityService;

    private ReactivationService reactivationService;

    public UserController(IdentityService identityService) {
        this.identityService = identityService;
    }


    @GetMapping("/sign-out")
    public String logoutUser(Principal principal, HttpServletRequest request) {
        request.getSession().invalidate();
        identityService.logoutUser();
        return "redirect:http://localhost:8080/logout?returnTo=http://localhost:8081/mgmt/login";
    }
}

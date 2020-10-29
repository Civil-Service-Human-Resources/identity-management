package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.cshr.service.security.IdentityService;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class UserController {

    private IdentityService identityService;

    @Value("${identity.signoutUrl}")
    private String signoutUrl;

    @Value("${identity.returnToUrl}")
    private String returnToUrl;

    public UserController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping("/sign-out")
    public String logoutUser(Principal principal, HttpServletRequest request) {
        request.getSession().invalidate();
        identityService.logoutUser();
        return "redirect:" + signoutUrl + "?returnTo=" + returnToUrl;
    }
}
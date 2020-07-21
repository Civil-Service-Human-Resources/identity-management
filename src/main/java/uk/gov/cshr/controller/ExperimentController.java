package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.service.security.IdentityService;

@Controller
@RequestMapping("/experiment")
public class ExperimentController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/new")
    public void callNewMethod() {
        identityService.experimentalTrackUserActivity();
    }

    @GetMapping("/old")
    public void callOldMethod() {
        identityService.trackUserActivity();
    }
}

package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.cshr.service.security.IdentityService;

@Slf4j
@Controller
@RequestMapping("/experiment")
public class ExperimentController {

    @Autowired
    private IdentityService identityService;

    @GetMapping("/new")
    public void callNewMethod() {
        log.info("Calling experimental trackUserActivity");
        identityService.experimentalTrackUserActivity();
        log.info("Called trackUserActivity");
    }

    @GetMapping("/old")
    public void callOldMethod() {
        log.info("Calling old trackUserActivity");
        identityService.trackUserActivity();
        log.info("Called trackUserActivity");
    }
}

package uk.gov.cshr.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).READ_IDENTITY)")
@Slf4j
public class ManagementController {
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}

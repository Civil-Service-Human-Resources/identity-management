package uk.gov.cshr.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManagementController {
    @RequestMapping("/")
    public String index(){
        return "index";
    }
}

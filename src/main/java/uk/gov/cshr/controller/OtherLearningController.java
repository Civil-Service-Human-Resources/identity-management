package uk.gov.cshr.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.cshr.config.CustomOAuth2Authentication;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;
import uk.gov.cshr.repository.IdentityRepository;
import uk.gov.cshr.service.Pagination;
import uk.gov.cshr.service.cslService.CslService;
import uk.gov.cshr.service.cslService.models.GetOptionalLearningRecordParams;

import java.net.URI;

@Controller
@PreAuthorize("hasPermission(returnObject, T(uk.gov.cshr.config.Permission).READ_IDENTITY)")
@RequestMapping("/identities/update/{uid}/other-learning")
public class OtherLearningController extends BaseIdentityController {

    private final Integer pageSize;
    private final CslService cslService;

    public OtherLearningController(IdentityRepository identityRepository, @Value("${pagination.pageSize}") Integer pageSize, CslService cslService) {
        super(identityRepository);
        this.pageSize = pageSize;
        this.cslService = cslService;
    }

    @GetMapping()
    public String identityOtherLearning(Model model,
                                        @PathVariable String uid,
                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "query", required = false, defaultValue = "") String query,
                                        CustomOAuth2Authentication auth) {
        Identity identity = getIdentity(model, uid, auth, "other learning");
        if(identity == null) {
            return REDIRECT_IDENTITIES_LIST;
        }

        GetOptionalLearningRecordParams params = new GetOptionalLearningRecordParams(page, pageSize, query);

        UserLearningResponse response = cslService.getOtherLearningForUser(uid, params);

        if (response != null && response.getLearning() != null) {
            model.addAttribute("learningCourses", response.getLearning());
            int totalPages = (int) Math.ceil((double) response.getTotalResults() / pageSize);
            model.addAttribute("pagination", Pagination.generateList(response.getPage(), totalPages));
            model.addAttribute("currentPage", response.getPage());
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalResults", response.getTotalResults());
            model.addAttribute("query", params.getQ());
        }

        model.addAttribute("activeTab", "other-learning");
        return "identity/other-learning";
    }

    @GetMapping("/{courseId}")
    public String identityOtherLearningDetail(Model model,
                                              @PathVariable String uid,
                                              @PathVariable String courseId,
                                              CustomOAuth2Authentication auth, @RequestHeader(value = "referer", required = false) URI referer) {
        Identity identity = getIdentity(model, uid, auth, "other learning detail");
        if(identity == null) {
            return REDIRECT_IDENTITIES_LIST;
        }

        Learning learning = cslService.getDetailedLearningForUser(uid, courseId);
        if (learning == null || learning.getCourses() == null || learning.getCourses().isEmpty()) {
            model.addAttribute("error", "Course details not found");
        } else {
            model.addAttribute("course", learning.getCourses().get(0));
        }

        String backLink = String.format("/mgmt/identities/update/%s/other-learning", uid);
        model.addAttribute("backLinkText", "Back");
        model.addAttribute("backLink", backLink);
        if (referer != null && referer.getPath().startsWith(backLink)) {
            model.addAttribute("backLinkText", "Find another course");
            model.addAttribute("backLink", referer);
        }

        model.addAttribute("activeTab", "other-learning");
        return "identity/other-learning-detail";
    }

}

package uk.gov.cshr.utils.stub;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.stereotype.Service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Service
public class CslServiceStubService {

    public void getRequiredLearningForCourses(String dueDate, String response) {
        stubFor(
                WireMock.get(urlPathEqualTo("/csl_service/api/learning/required/for-departments"))
                        .withQueryParam("dueDate", equalTo(dueDate))
                        .withHeader("Authorization", equalTo("Bearer token"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(response))
        );
    }

}

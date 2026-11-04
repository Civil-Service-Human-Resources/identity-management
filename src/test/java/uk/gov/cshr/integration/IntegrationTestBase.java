package uk.gov.cshr.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.cshr.config.TestConfig;
import uk.gov.cshr.utils.stub.StubService;

@ActiveProfiles({"h2", "wiremock"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestConfig.class})
public class IntegrationTestBase extends CSLServiceWireMockServer {

    @Autowired
    protected StubService stubService;

}

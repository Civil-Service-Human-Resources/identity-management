package uk.gov.cshr;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;
import uk.gov.cshr.service.CSRSService;
import uk.gov.cshr.service.RequestEntityFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CSRSServiceTest {

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    
    @Autowired
    private CSRSService classUnderTest;

    @MockBean
    private RequestEntityFactory requestEntityFactory;

    @Before
    public void setUp() throws URISyntaxException {
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);

        RequestEntity getRequestEntity = new RequestEntity(HttpMethod.GET, new URI("/civilServants/orgcode"));
        when(requestEntityFactory.createGetRequest(anyString())).thenReturn(getRequestEntity);
    }

    @Test
    public void givenAValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsSuccessfully() {

        this.mockServer.expect(requestTo("/civilServants/orgcode"))
                .andRespond(withSuccess("co", MediaType.APPLICATION_JSON));

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        mockServer.verify();

        assertThat(actual).isNotNull();
        assertThat(actual.getBody().toString()).isEqualTo("co");
    }

    @Test
    public void givenAInvalidValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsNotFound() {

        this.mockServer.expect(requestTo("/civilServants/orgcode"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        mockServer.verify();

        assertThat(actual).isNull();
    }

}

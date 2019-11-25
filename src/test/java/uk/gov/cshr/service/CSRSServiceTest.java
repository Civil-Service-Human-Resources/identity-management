package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;
import uk.gov.cshr.utils.JsonUtils;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

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

    private static final String CODE = "aCode";

    private static final String DOMAIN = "aDomain";

    private static final String EXPECTED_GET_AGENCYTOKEN_FOR_CIVIL_SERVANT_BY_DOMAIN_AND_CODE_URL = "/agencyTokens/?domain=aDomain&code=aCode";

    private static final String EXPECTED_GET_ORG_CODE_FOR_CIVIL_SERVANT_URL = "/civilServants/orgcode";

    @Before
    public void setUp() throws URISyntaxException {
        RestGatewaySupport gateway = new RestGatewaySupport();
        gateway.setRestTemplate(restTemplate);
        mockServer = MockRestServiceServer.createServer(gateway);

        RequestEntity getOrgCodeRequestEntity = new RequestEntity(HttpMethod.GET, new URI("/civilServants/orgcode"));
        String getOrgCodeURL = getOrgCodeRequestEntity.getUrl().toString();
        when(requestEntityFactory.createGetRequest(contains(getOrgCodeURL))).thenReturn(getOrgCodeRequestEntity);

        RequestEntity getAgencyTokenByDomainAndCodeRequestEntity = new RequestEntity(HttpMethod.GET, new URI("/agencyTokens/?domain=aDomain&code=aCode"));
        String getAgencyTokenByDomainAndCodeURL = getAgencyTokenByDomainAndCodeRequestEntity.getUrl().toString();
        when(requestEntityFactory.createGetRequest(contains(getAgencyTokenByDomainAndCodeURL))).thenReturn(getAgencyTokenByDomainAndCodeRequestEntity);
    }

    @Test
    public void givenAValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsSuccessfully() {

        this.mockServer.expect(requestTo(EXPECTED_GET_ORG_CODE_FOR_CIVIL_SERVANT_URL))
                .andRespond(withSuccess("co", MediaType.APPLICATION_JSON));

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        mockServer.verify();
        assertThat(actual).isNotNull();
        assertThat(actual.getBody().toString()).isEqualTo("co");
    }

    @Test
    public void givenAInvalidValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsNotFound() {

        this.mockServer.expect(requestTo(EXPECTED_GET_ORG_CODE_FOR_CIVIL_SERVANT_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        mockServer.verify();
        assertThat(actual).isNull();
    }

    @Test
    public void givenAnIssueCreatingRequest_whenGetOrganisationCodeForCivilServant_thenDoesNotCallExternalServiceAndReturnsNull() {

        when(requestEntityFactory.createGetRequest(anyString())).thenThrow(new NullPointerException());

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        assertThat(actual).isNull();
    }

    @Test
    public void givenAValidDomainAndCode_whenGetAgencyTokenForCivilServant_thenReturnsSuccessfully() {

        AgencyTokenResponseDTO responseDTO = new AgencyTokenResponseDTO();
        responseDTO.setCapacity(100);
        responseDTO.setCapacityUsed(20);
        responseDTO.setToken("aToken");
        responseDTO.setId(new Long(1));
        String responseDTOAsAJsonString = JsonUtils.asJsonString(responseDTO);
        this.mockServer.expect(requestTo(EXPECTED_GET_AGENCYTOKEN_FOR_CIVIL_SERVANT_BY_DOMAIN_AND_CODE_URL))
                .andRespond(withSuccess(responseDTOAsAJsonString, MediaType.APPLICATION_JSON));

        ResponseEntity actual = this.classUnderTest.getAgencyTokenForCivilServant("aDomain", "aCode");

        mockServer.verify();
        assertThat(actual).isNotNull();

        AgencyTokenResponseDTO actualDTO = (AgencyTokenResponseDTO) actual.getBody();
        assertThat(actualDTO.getToken()).isEqualTo("aToken");
        assertThat(actualDTO.getCapacity()).isEqualTo(100);
        assertThat(actualDTO.getCapacityUsed()).isEqualTo(20);
        assertThat(actualDTO.getId()).isEqualTo(1l);
    }

    @Test
    public void givenAnDomainAndCodeThatIsNotFound_whenGetAgencyTokenForCivilServant_thenReturnsNotFound() {

        this.mockServer.expect(requestTo(EXPECTED_GET_AGENCYTOKEN_FOR_CIVIL_SERVANT_BY_DOMAIN_AND_CODE_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity actual = this.classUnderTest.getAgencyTokenForCivilServant("aDomain", "aCode");

        mockServer.verify();
        assertThat(actual).isNull();
    }

    @Test
    public void givenAnIssueCreatingRequest_whenGetAgencyTokenForCivilServant_thenReturnsNull() {

        when(requestEntityFactory.createGetRequest(anyString())).thenThrow(new NullPointerException());

        ResponseEntity actual = this.classUnderTest.getAgencyTokenForCivilServant("aDomain", "aCode");

        assertThat(actual).isNull();
    }

}

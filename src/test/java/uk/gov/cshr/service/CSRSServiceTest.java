package uk.gov.cshr.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;
import uk.gov.cshr.dto.AgencyTokenResponseDTO;
import uk.gov.cshr.dto.UpdateSpacesForAgencyTokenRequestDTO;
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

    @Captor
    private ArgumentCaptor<UpdateSpacesForAgencyTokenRequestDTO> updateAgencyTokenRequestDTO;

    private static final String EXPECTED_GET_AGENCYTOKEN_FOR_CIVIL_SERVANT_BY_DOMAIN_AND_CODE_URL = "/agencyTokens/?domain=aDomain&code=aCode";

    private static final String EXPECTED_GET_ORG_CODE_FOR_CIVIL_SERVANT_URL = "/civilServants/org";

    private static final String EXPECTED_PUT_UPDATE_AGENCYTOKEN_URL = "/agencyTokens";

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

        RequestEntity updateAgencyTokenSpacesRequestEntity = new RequestEntity(HttpMethod.PUT, new URI("/agencyTokens"));
        String updateAgencyTokenURL = updateAgencyTokenSpacesRequestEntity.getUrl().toString();
        UpdateSpacesForAgencyTokenRequestDTO updateAgencyTokenSpacesDTO = new UpdateSpacesForAgencyTokenRequestDTO();
        updateAgencyTokenSpacesDTO.setCode("aCode");
        updateAgencyTokenSpacesDTO.setDomain("aDomain");
        updateAgencyTokenSpacesDTO.setToken("aToken");
        updateAgencyTokenSpacesDTO.setRemoveUser(false);
        when(requestEntityFactory.createPutRequest(contains(updateAgencyTokenURL), updateAgencyTokenRequestDTO.capture())).thenReturn(updateAgencyTokenSpacesRequestEntity);
    }

    @Test
    public void givenAValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsSuccessfully() throws URISyntaxException {

        RequestEntity getOrgCodeRequestEntity = new RequestEntity(HttpMethod.GET, new URI("/civilServants/org"));
        String getOrgCodeURL = getOrgCodeRequestEntity.getUrl().toString();
        when(requestEntityFactory.createGetRequest(contains(getOrgCodeURL))).thenReturn(getOrgCodeRequestEntity);

        this.mockServer.expect(requestTo(EXPECTED_GET_ORG_CODE_FOR_CIVIL_SERVANT_URL))
                .andRespond(withSuccess("co", MediaType.APPLICATION_JSON));

        ResponseEntity actual = this.classUnderTest.getOrganisationCodeForCivilServant("myUId");

        mockServer.verify();
        assertThat(actual).isNotNull();
        assertThat(actual.getBody().toString()).isEqualTo("co");
    }

    @Test
    public void givenAInvalidValidUID_whenGetOrganisationCodeForCivilServant_thenReturnsNotFound() throws URISyntaxException {

        RequestEntity getOrgCodeRequestEntity = new RequestEntity(HttpMethod.GET, new URI("/civilServants/org"));
        String getOrgCodeURL = getOrgCodeRequestEntity.getUrl().toString();
        when(requestEntityFactory.createGetRequest(contains(getOrgCodeURL))).thenReturn(getOrgCodeRequestEntity);

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

    @Test
    public void givenAValidCodeAndDomainAndToken_whenUpdateAgencyTokenForCivilServant_thenReturnsSuccessfully() {

        AgencyTokenResponseDTO responseDTO = new AgencyTokenResponseDTO();
        responseDTO.setCapacity(100);
        responseDTO.setCapacityUsed(20);
        responseDTO.setToken("token123");
        responseDTO.setId(new Long(1));
        String responseDTOAsAJsonString = JsonUtils.asJsonString(responseDTO);
        this.mockServer.expect(requestTo(EXPECTED_PUT_UPDATE_AGENCYTOKEN_URL))
                .andRespond(withSuccess(responseDTOAsAJsonString, MediaType.APPLICATION_JSON));

        ResponseEntity actual = this.classUnderTest.updateAgencyTokenForCivilServant("aCode", "aDomain", "aToken", false);

        mockServer.verify();

        // check request parameters were put into request dto correctly
        UpdateSpacesForAgencyTokenRequestDTO actualRequestDTO = updateAgencyTokenRequestDTO.getValue();
        assertThat(actualRequestDTO.getCode()).isEqualTo("aCode");
        assertThat(actualRequestDTO.getDomain()).isEqualTo("aDomain");
        assertThat(actualRequestDTO.getToken()).isEqualTo("aToken");
        assertThat(actualRequestDTO.isRemoveUser()).isFalse();

        // check response is correct
        assertThat(actual).isNotNull();

        AgencyTokenResponseDTO actualDTO = (AgencyTokenResponseDTO) actual.getBody();
        assertThat(actualDTO.getToken()).isEqualTo("token123");
        assertThat(actualDTO.getCapacity()).isEqualTo(100);
        assertThat(actualDTO.getCapacityUsed()).isEqualTo(20);
        assertThat(actualDTO.getId()).isEqualTo(1l);
    }

    @Test(expected = HttpClientErrorException.class)
    public void givenAnInvalidCode_whenUpdateAgencyTokenForCivilServant_thenTheActualExceptionIsThrownAndNotCaught() {

        this.mockServer.expect(requestTo(EXPECTED_PUT_UPDATE_AGENCYTOKEN_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseEntity actual = this.classUnderTest.updateAgencyTokenForCivilServant("aCode", "aDomain", "aToken", false);

        mockServer.verify();
    }

    @Test(expected = NullPointerException.class)
    public void givenAnIssueCreatingRequest_whenUpdateAgencyTokenForCivilServant_thenTheActualExceptionIsThrownAndNotCaught() {

        when(requestEntityFactory.createPutRequest(anyString(), any())).thenThrow(new NullPointerException());

        ResponseEntity actual = this.classUnderTest.updateAgencyTokenForCivilServant("aCode", "aDomain", "aToken", false);
    }

}

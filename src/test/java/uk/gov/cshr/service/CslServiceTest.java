package uk.gov.cshr.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.cshr.client.HttpClient;
import uk.gov.cshr.domain.learning.Learning;
import uk.gov.cshr.domain.learning.UserLearningResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CslServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private RequestEntityFactory requestEntityFactory;

    private CslService cslService;

    private final String getRequiredLearningUrl = "http://localhost/required-learning";
    private final String getFormattedOrganisationNamesUrl = "http://localhost/org-names";
    private final String userLearningUrl = "http://localhost/user-learning";
    private final String getDetailedLearningUrl = "http://localhost/detailed-learning";

    @Before
    public void setUp() {
        cslService = new CslService(httpClient, requestEntityFactory, getRequiredLearningUrl, getFormattedOrganisationNamesUrl, userLearningUrl, getDetailedLearningUrl);
    }

    @Test
    public void getUserLearning_returnsResponse() {
        String uid = "uid";
        int page = 0;
        int size = 20;
        String expectedUrl = String.format("%s/%s?page=%d&size=%d", userLearningUrl, uid, page, size);

        RequestEntity mockRequestEntity = mock(RequestEntity.class);
        when(requestEntityFactory.createGetRequest(expectedUrl)).thenReturn(mockRequestEntity);

        UserLearningResponse expectedResponse = new UserLearningResponse();
        ResponseEntity<UserLearningResponse> responseEntity = ResponseEntity.ok(expectedResponse);
        when(httpClient.sendRequestNoRetries(mockRequestEntity, UserLearningResponse.class)).thenReturn(responseEntity);

        UserLearningResponse actualResponse = cslService.getOtherLearningForUser(uid, page, size);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    public void getUserLearning_returnsNullOn404() {
        String uid = "uid";
        int page = 0;
        int size = 20;
        String expectedUrl = String.format("%s/%s?page=%d&size=%d", userLearningUrl, uid, page, size);

        RequestEntity mockRequestEntity = mock(RequestEntity.class);
        when(requestEntityFactory.createGetRequest(expectedUrl)).thenReturn(mockRequestEntity);

        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getRawStatusCode()).thenReturn(404);
        when(httpClient.sendRequestNoRetries(mockRequestEntity, UserLearningResponse.class)).thenThrow(exception);

        UserLearningResponse actualResponse = cslService.getOtherLearningForUser(uid, page, size);

        assertNull(actualResponse);
    }

    @Test(expected = RestClientResponseException.class)
    public void getUserLearning_throwsExceptionOn500() {
        String uid = "uid";
        int page = 0;
        int size = 20;
        String expectedUrl = String.format("%s/%s?page=%d&size=%d", userLearningUrl, uid, page, size);

        RequestEntity mockRequestEntity = mock(RequestEntity.class);
        when(requestEntityFactory.createGetRequest(expectedUrl)).thenReturn(mockRequestEntity);

        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getRawStatusCode()).thenReturn(500);
        when(httpClient.sendRequestNoRetries(mockRequestEntity, UserLearningResponse.class)).thenThrow(exception);

        cslService.getOtherLearningForUser(uid, page, size);
    }

    @Test
    public void getDetailedLearning_returnsResponse() {
        String uid = "uid";
        String courseId = "courseId";
        String expectedUrl = String.format("%s/%s?courseIds=%s", getDetailedLearningUrl, uid, courseId);

        RequestEntity mockRequestEntity = mock(RequestEntity.class);
        when(requestEntityFactory.createGetRequest(expectedUrl)).thenReturn(mockRequestEntity);

        Learning expectedResponse = new Learning();
        ResponseEntity<Learning> responseEntity = ResponseEntity.ok(expectedResponse);
        when(httpClient.sendRequestNoRetries(mockRequestEntity, Learning.class)).thenReturn(responseEntity);

        Learning actualResponse = cslService.getDetailedLearningForUser(uid, courseId);

        assertEquals(expectedResponse, actualResponse);
    }
}

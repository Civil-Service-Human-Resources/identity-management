package uk.gov.cshr.client;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestHttpClient {

    @Mock
    private RestTemplate restTemplate;

    /*
    * Test that when all retries are used up, an exception is thrown
    * */
    @Test(expected = RequestEntityException.class)
    public void testRetryFail() {
        RequestEntity requestEntity = mock(RequestEntity.class);

        when(restTemplate.exchange(requestEntity, Void.class))
                .thenThrow(RequestEntityException.class)
                .thenThrow(RequestEntityException.class)
                .thenThrow(RequestEntityException.class);

        HttpClient clientUnderTest = new HttpClient(restTemplate);

        ResponseEntity<Void> response = clientUnderTest.sendRequest(requestEntity, Void.class);
        verify(restTemplate, times(3)).exchange(requestEntity, Void.class);
    }


    /*
     * Test that when all retries are used up, a null value is returned.
     * */
    @Test
    public void testRetry() {

        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(
                header,
                HttpStatus.OK
        );

        when(restTemplate.exchange(null, Void.class))
                .thenThrow(RequestEntityException.class)
                .thenReturn(responseEntity);

        HttpClient clientUnderTest = new HttpClient(restTemplate);

        ResponseEntity<Void> response = clientUnderTest.sendRequest(null, Void.class);

        verify(restTemplate, times(2)).exchange(null, Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

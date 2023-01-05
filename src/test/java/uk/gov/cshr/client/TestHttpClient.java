package uk.gov.cshr.client;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import uk.gov.cshr.service.RequestEntityException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestHttpClient {

    @Mock
    private RestTemplate restTemplate;

    /*
    * Test that when all retries are used up, a null value is returned.
    * */
    @Test
    public void testRetryFail() {

        when(restTemplate.exchange(any(), Void.class))
                .thenThrow(RequestEntityException.class)
                .thenThrow(RequestEntityException.class)
                .thenThrow(RequestEntityException.class);

        HttpClient clientUnderTest = new HttpClient(restTemplate);

        ResponseEntity<Void> response = clientUnderTest.sendRequest(null);

        verify(restTemplate, times(3)).exchange(any(), Void.class);
        assertNull(response);
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

        when(restTemplate.exchange(any(), Void.class))
                .thenThrow(RequestEntityException.class)
                .thenReturn(responseEntity);

        HttpClient clientUnderTest = new HttpClient(restTemplate);

        ResponseEntity<Void> response = clientUnderTest.sendRequest(null);

        verify(restTemplate, times(2)).exchange(any(), Void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

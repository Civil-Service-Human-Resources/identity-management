package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.repository.ReactivationRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReactivationServiceTest {

    private static final String EMAIL = "test@example.com";

    @Mock
    private ReactivationRepository reactivationRepository;

    @InjectMocks
    private ReactivationService reactivationService;

    @Test
    public void createReactivationRequest() {
        Reactivation reactivation = new Reactivation();

        when(reactivationRepository.save(any(Reactivation.class))).thenReturn(reactivation);

        assertEquals(reactivationService.createReactivationRequest(EMAIL), reactivation);
    }
}
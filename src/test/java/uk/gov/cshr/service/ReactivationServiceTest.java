package uk.gov.cshr.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.cshr.domain.Reactivation;
import uk.gov.cshr.domain.ReactivationStatus;
import uk.gov.cshr.repository.ReactivationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    @Test
    public void testGetLatestReactivationNull() {
        when(reactivationRepository.findByEmailAndReactivationStatusEquals("email.com", ReactivationStatus.REACTIVATED))
                .thenReturn(Collections.emptyList());
        assertNull(reactivationService.getLatestReactivationForEmail("email.com"));
    }

    @Test
    public void testGetLatestReactivation() {
        Instant instant = Instant.now();
        Date date = new Date(instant.toEpochMilli());
        Date date2 = new Date(instant.plus(1L, ChronoUnit.DAYS).toEpochMilli());
        Reactivation reactivation = new Reactivation();
        reactivation.setReactivatedAt(date);
        Reactivation reactivation2 = new Reactivation();
        reactivation2.setReactivatedAt(date2);
        when(reactivationRepository.findByEmailAndReactivationStatusEquals("email.com", ReactivationStatus.REACTIVATED))
                .thenReturn(Arrays.asList(reactivation, reactivation2));
        assertEquals(date2, reactivationService.getLatestReactivationForEmail("email.com"));
    }
}

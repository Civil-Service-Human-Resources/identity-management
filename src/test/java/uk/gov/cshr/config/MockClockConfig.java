package uk.gov.cshr.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class MockClockConfig {


    @Bean
    @Primary
    public Clock getMockClock() {
        return MockClockConfig.getClock();
    }

    public static Clock getClock() {
        return Clock.fixed(Instant.parse("2023-01-01T10:00:00.000Z"), ZoneId.of("UTC"));
    }

}

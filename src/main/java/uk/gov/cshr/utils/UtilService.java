package uk.gov.cshr.utils;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class UtilService implements IUtilService {

    private final Clock clock;

    public UtilService(Clock clock) {
        this.clock = clock;
    }

    @Override
    public LocalDateTime getNowDateTime() {
        return LocalDateTime.now(clock);
    }
}

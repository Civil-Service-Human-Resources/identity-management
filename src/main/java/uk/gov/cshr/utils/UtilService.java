package uk.gov.cshr.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UtilService implements IUtilService {

    private final Clock clock;

    @Override
    public LocalDateTime getNowDateTime() {
        return LocalDateTime.now(clock);
    }

    @Override
    public LocalDate getNowDate() {
        return LocalDate.now(clock);
    }

}

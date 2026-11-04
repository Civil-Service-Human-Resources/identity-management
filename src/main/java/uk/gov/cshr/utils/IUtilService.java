package uk.gov.cshr.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IUtilService {
    LocalDateTime getNowDateTime();

    LocalDate getNowDate();
}

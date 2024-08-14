package uk.gov.cshr.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String formatDatetimeForFE(Instant date) {
        return date == null ? "N/A" : date.atZone(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("dd/MM/y HH:mm:ss"));
    }

    public static String formatDateForFE(Instant date) {
        return date == null ? "N/A" : date.atZone(ZoneId.of("Europe/London")).format(DateTimeFormatter.ofPattern("dd/MM/y"));
    }

}

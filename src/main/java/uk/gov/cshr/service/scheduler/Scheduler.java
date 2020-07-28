package uk.gov.cshr.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityService;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class Scheduler {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private IdentityService identityService;

    @Scheduled(cron = "0 0 5 * * *")
    public void trackUserActivity() {
        log.info("Executing trackUserActivity at {}", dateFormat.format(new Date()));
        identityService.trackUserActivity();
        log.info("trackUserActivity complete at {}", dateFormat.format(new Date()));
    }
}

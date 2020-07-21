package uk.gov.cshr.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityService;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private IdentityService identityService;

    @Scheduled(cron = "0 0 5 * * *")
    public void trackUserActivity() {
        LOGGER.info("Executing trackUserActivity at {}", dateFormat.format(new Date()));
        identityService.trackUserActivity();
        LOGGER.info("trackUserActivity complete at {}", dateFormat.format(new Date()));
    }
}

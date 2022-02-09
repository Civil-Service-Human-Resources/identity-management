package uk.gov.cshr.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.dataRetentionJob.DataRetentionJobService;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class Scheduler {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Value("${dataRetentionJob.enabled}")
    private boolean retentionJobEnabled;

    private final DataRetentionJobService dataRetentionJobService;

    public Scheduler(DataRetentionJobService dataRetentionJobService) {
        this.dataRetentionJobService = dataRetentionJobService;
    }

    @Scheduled(cron = "${dataRetentionJob.cronSchedule}")
    public void trackUserActivity() {
        if (retentionJobEnabled) {
            log.info("Executing data retention job at {}", dateFormat.format(new Date()));
            dataRetentionJobService.runDataRetentionJob();
            log.info("data retention job complete at {}", dateFormat.format(new Date()));
        }
    }
}

package uk.gov.cshr.service.scheduler;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.service.security.IdentityService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SchedulerTest {

  @Autowired
  private Scheduler scheduler;

  @MockBean
  private IdentityService identityService;

  @Test
  public void trackUserActivity_verifyExpectedCalls() {
    scheduler.trackUserActivity();
    verify(identityService, times(1)).trackUserActivity();
  }
}
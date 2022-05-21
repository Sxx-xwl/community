package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author sxx_27
 * @create 2022-05-21 17:33
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class QuartzTests {
    
    @Autowired
    private Scheduler scheduler;
    
    @Test
    public void test(){
        try {
            System.out.println(scheduler.deleteJob(new JobKey("alphaJob", "alphaJobGroup")));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
    
}

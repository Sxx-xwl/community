package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.service.AlphaService;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author sxx_27
 * @create 2022-05-21 15:51
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolTest.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    //JDK可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    private void sleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //jdk普通线程池
    @Test
    public void test() {

        Runnable take = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello executorService");
            }
        };

        for (int i = 0; i < 10; i++) {
            executorService.submit(take);
        }

        sleep(10000);
    }

    //JDK可执行定时任务的线程池
    @Test
    public void test1() {

        Runnable take = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello scheduledExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(take, 10000, 1000, TimeUnit.MILLISECONDS);

        sleep(30000);
    }

    //spring普通线程池
    @Test
    public void test2(){
        Runnable take = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello taskExecutor");
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(take);
        }

        sleep(10000);
    }

    //spring可执行定时任务的线程池
    @Test
    public void test3() {

        Runnable take = new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("hello taskScheduler");
            }
        };
        Date startTime = new Date(System.currentTimeMillis() + 10000);
        taskScheduler.scheduleAtFixedRate(take,startTime,1000);
        sleep(30000);
    }

    //spring普通线程池简化使用方式
    @Test
    public void test4(){
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }
        sleep(10000);
    }

    //spring可执行定时任务的线程池简化使用方式
    @Test
    public void test5(){
        sleep(30000);
    }
}

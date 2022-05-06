package com.twk.nccommunity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;


@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class ThreadPool {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    private void sleep(long n){
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test(){
        Runnable task = () -> logger.debug("Running SpringExecutor");
        for(int i = 0;i<10;i++){
            executor.submit(task);
        }
        sleep(10000);
    }

    @Test
    public void testSchedule(){
        Runnable task = () -> logger.debug("Running SpringExecutor");
        Date date = new Date(System.currentTimeMillis() + 10000);
        scheduler.scheduleAtFixedRate(task,date,1000);
        sleep(10000);
    }
}

package com.twk.nccommunity.config;

import com.twk.nccommunity.quartz.AlphaJob;
import com.twk.nccommunity.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {

    //刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("CommunityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreJobTrigger(JobDetail postScoreJobDetail){
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(postScoreJobDetail);
        triggerFactoryBean.setName("postScoreJobRefreshTrigger");
        triggerFactoryBean.setGroup("CommunityTriggerGroup");
        //5分钟执行一次
        triggerFactoryBean.setRepeatInterval(1000 * 60 * 5);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }


}

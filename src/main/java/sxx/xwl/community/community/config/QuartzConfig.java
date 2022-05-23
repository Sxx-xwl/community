package sxx.xwl.community.community.config;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import sxx.xwl.community.community.quartz.AlphaJob;
import sxx.xwl.community.community.quartz.PostScoreRefreshJob;

/**
 * @author sxx_27
 * @create 2022-05-21 16:56
 */
@Configuration
public class QuartzConfig {

    //    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);//永久保存
        factoryBean.setRequestsRecovery(true);//可恢复
        return factoryBean;
    }

    //    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);//间隔时间
        factoryBean.setJobDataMap(new JobDataMap());//存储方式
        return factoryBean;
    }

    //刷新帖子分数
    @Bean
    public JobDetailFactoryBean postScorRefreshDetial() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);//永久保存
        factoryBean.setRequestsRecovery(true);//可恢复
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScorRefreshTrigger(JobDetail postScorRefreshDetial) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScorRefreshDetial);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);//间隔时间
        factoryBean.setJobDataMap(new JobDataMap());//存储方式
        return factoryBean;
    }
}

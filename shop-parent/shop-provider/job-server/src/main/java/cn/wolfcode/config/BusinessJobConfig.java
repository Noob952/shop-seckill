package cn.wolfcode.config;

import cn.wolfcode.job.MyInitSeckillProductJob;
import cn.wolfcode.job.UserCacheJob;
import cn.wolfcode.util.ElasticJobUtil;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessJobConfig {
    @Bean(initMethod = "init")
    public SpringJobScheduler initUserCacheJob(CoordinatorRegistryCenter registryCenter, UserCacheJob userCacheJob){
        LiteJobConfiguration jobConfiguration = ElasticJobUtil.createDefaultSimpleJobConfiguration(userCacheJob.getClass(), userCacheJob.getCron());
        SpringJobScheduler springJobScheduler = new SpringJobScheduler(userCacheJob, registryCenter,jobConfiguration );
        return springJobScheduler;
    }

    @Bean(initMethod = "init")
    public SpringJobScheduler initSeckillProductJob(CoordinatorRegistryCenter registryCenter, MyInitSeckillProductJob myInitSeckillProductJob){
        LiteJobConfiguration jobConfiguration = ElasticJobUtil.createJobConfiguration(myInitSeckillProductJob.getClass(), myInitSeckillProductJob.getCron(),3,"0=10,1=12,2=14",false);
        SpringJobScheduler springJobScheduler = new SpringJobScheduler(myInitSeckillProductJob, registryCenter,jobConfiguration );
        return springJobScheduler;
    }
}

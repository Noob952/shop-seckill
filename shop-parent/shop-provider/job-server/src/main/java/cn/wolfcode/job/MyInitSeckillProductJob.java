package cn.wolfcode.job;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.SeckillProductFeignApi;
import cn.wolfcode.redis.JobRedisKey;
import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class MyInitSeckillProductJob implements SimpleJob {

    @Autowired
    private SeckillProductFeignApi seckillProductFeignApi;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${jobCron.initSeckillProduct}")
    private String cron;

    @Override
    public void execute(ShardingContext shardingContext) {
        //System.out.println(shardingContext.getShardingParameter());
        doWork(shardingContext.getShardingParameter());

    }

    private void doWork(String time) {
        //1远程调用秒杀服务把数据查出来Result data:List<SeckillProductVO>=
        Result<List<SeckillProductVo>> result = seckillProductFeignApi.queryBYTime(Integer.parseInt(time));
        /**
         * result的数据一共有几种情况
         * 1 第一种情况秒杀服务直接挂掉了，直接走降级的方法返回值是null
         * 2 第二种情况调用没问题 {code:200,msg:操作成功, data:List<SeckillProductVo>}
         * 3 第三种情况 秒杀服务内部出现问题{code:50201,msg:秒杀服务繁忙}
         */
        if (result!=null&& !result.hasError()){
            List<SeckillProductVo> seckillProductVos = result.getData();
            //2把查询来的数据放到redis当中
            if (seckillProductVos!=null&&seckillProductVos.size()>0){
                for (SeckillProductVo seckillProductVo : seckillProductVos) {
                    redisTemplate.opsForHash().put(JobRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time),
                            seckillProductVo.getId()+"",JSON.toJSONString(seckillProductVo));
                    redisTemplate.opsForHash().put(JobRedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(time),
                            seckillProductVo.getId()+"",seckillProductVo.getStockCount()+"");
                 }
            }
        }

    }
}

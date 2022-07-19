package cn.wolfcode.mq;

import cn.wolfcode.service.IOrderInfoService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        consumerGroup = "MQCreateOrderListener",
        topic = MQConstant.ORDER_PEDDING_TOPIC
)
public class MQCreateOrderListener implements RocketMQListener<OrderMessage> {

    @Autowired
    private IOrderInfoService orderInfoService;
    @Override
    public void onMessage(OrderMessage orderMessage) {
        Long seckillId = orderMessage.getSeckillId();
        Integer time = orderMessage.getTime();
        Long userPhone = orderMessage.getUserPhone();
        String token = orderMessage.getToken();

        //执行秒杀业务逻辑
        try {
            orderInfoService.doSeckill(seckillId,time,userPhone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package cn.wolfcode.mq;

import cn.wolfcode.config.WebSocketServer;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RocketMQMessageListener(
        consumerGroup = "MQResultListener",
        topic = MQConstant.ORDER_RESULT_TOPIC
)
@Slf4j
public class MQResultListener implements RocketMQListener<OrderMQResult> {


    @Override
    public void onMessage(OrderMQResult orderMQResult) {
        log.info("进入到通知消费者中...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //找到客户端
        WebSocketServer webSocketServer = WebSocketServer.clients.get(orderMQResult.getToken());
        if (webSocketServer!=null){
            //给客户端去发送数据
            try {
                webSocketServer.getSession().getBasicRemote().sendText(JSON.toJSONString(orderMQResult));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

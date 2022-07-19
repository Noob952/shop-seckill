package cn.wolfcode.web.controller;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.DateUtil;
import cn.wolfcode.util.UserUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;

    public static ConcurrentHashMap<Long, Boolean> local_flag = new ConcurrentHashMap<>();

    @RequestMapping("/doSeckill")
    @RequireLogin
    public Result<String> doSeckill(int time, Long seckillId, HttpServletRequest request) {
        //1短短用户是否有登录
        //2做参数校验
        if (StringUtils.isEmpty(time + "") || StringUtils.isEmpty(seckillId + "")) {
            throw new BusinessException(SeckillCodeMsg.OPT_ERROR);
        }
        SeckillProductVo seckillProductVo = seckillProductService.find(time, seckillId);
        //判断本地标识是否为true ，如果为true表示redis已经减到负数
        Boolean isFlag = local_flag.get(seckillId);
        if (isFlag!=null&&isFlag){
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        //3 判断商品是否在活动时间内
//        boolean isFlag = DateUtil.isLegalTime(seckillProductVo.getStartDate(), time);
//        if (!isFlag) {
//            throw new BusinessException(SeckillCodeMsg.OPT_ERROR);
//        }
        //4 判断库存是否足够
        Integer count = seckillProductVo.getStockCount();
        if (count <= 0) {
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        //5 判断用户是否重复下单
        //利用redis控制进入到service的人数
        Long stockCount = redisTemplate.opsForHash().increment(SeckillRedisKey.SECKILL_REAL_COUNT_HASH.getRealKey(time + ""), seckillId + "", -1);
        if (stockCount < 0) {
            local_flag.put(seckillId, true);
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        String token = request.getHeader("token");
        UserInfo user = UserUtil.getUser(redisTemplate, token);
        Boolean flag = redisTemplate.opsForSet().isMember(SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(time + ""), seckillId + ":" + user.getPhone());
        if (flag) {
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        }
        //6 秒杀业务逻辑
        //String orderNo = orderInfoService.doSeckill(seckillId, time, user.getPhone());
        OrderMessage orderMessage = new OrderMessage();
        orderMessage.setSeckillId(seckillId);
        orderMessage.setTime(time);
        orderMessage.setToken(token);
        orderMessage.setUserPhone(user.getPhone());
        rocketMQTemplate.sendOneWay(MQConstant.ORDER_PEDDING_TOPIC,orderMessage);
        return Result.success("正在抢购中，请稍后...");
    }
}

package cn.wolfcode.service;


import cn.wolfcode.domain.OrderInfo;

import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
public interface IOrderInfoService {

    /**
     * 秒杀业务逻辑
     * @param seckillId 秒杀商品id
     * @param time 场次
     * @param phone 手机号
     * @return
     */
    String doSeckill(Long seckillId, int time, Long phone);
}

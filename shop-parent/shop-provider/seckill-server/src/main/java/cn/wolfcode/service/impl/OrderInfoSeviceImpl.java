package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class OrderInfoSeviceImpl implements IOrderInfoService {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;

    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Override
    @Transactional
    public String doSeckill(Long seckillId, int time, Long phone) {
        //1减少真实库存
        int count = seckillProductMapper.decrStock(seckillId);
        if (count==0){
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        //2创建订单并把订单保存到数据库中
        String orderNo=createOrder(seckillId,time,phone);
        //3往redis
        redisTemplate.opsForSet().add(SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(time+""),seckillId+":"+phone);
        return orderNo;
    }

    private String createOrder(Long seckillId, int time, Long phone) {
        SeckillProductVo seckillProductVo = seckillProductService.find(time, seckillId);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(1L);
        orderInfo.setIntergral(seckillProductVo.getIntergral());
        orderInfo.setProductCount(seckillProductVo.getStockCount());
        orderInfo.setProductId(seckillProductVo.getProductId());
        orderInfo.setProductImg(seckillProductVo.getProductImg());
        orderInfo.setProductName(seckillProductVo.getProductName());
        orderInfo.setProductPrice(seckillProductVo.getProductPrice());
        orderInfo.setSeckillPrice(seckillProductVo.getSeckillPrice());
        orderInfo.setSeckillDate(new Date());
        orderInfo.setSeckillId(seckillId);
        orderInfo.setSeckillTime(time);
        orderInfo.setUserId(phone);
        orderInfo.setOrderNo(IdGenerateUtil.get().nextId()+"");
        orderInfoMapper.insert(orderInfo);
        return orderInfo.getOrderNo();
    }
}

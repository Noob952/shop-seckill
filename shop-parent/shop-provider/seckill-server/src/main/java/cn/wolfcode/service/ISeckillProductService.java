package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;

import java.util.List;


public interface ISeckillProductService {
    /**
     * 根据场次把秒杀列表数据查询出来
     * @param time 场次
     * @return
     */
    List<SeckillProductVo> selectByTime(int time);

    /**
     * 前台界面展示秒杀列表数据
     * @param time
     * @return
     */
    List<SeckillProductVo> queryByTime(int time);

    /**
     * 根据场次和秒杀商品id查询商品详情
     * @param time 场次
     * @param seckillId 商品id
     * @return
     */
    SeckillProductVo find(int time, Long seckillId);
}

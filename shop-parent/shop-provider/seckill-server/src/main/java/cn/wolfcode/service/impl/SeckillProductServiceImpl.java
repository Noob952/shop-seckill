package cn.wolfcode.service.impl;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.feign.ProductFeignApi;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.ISeckillProductService;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class SeckillProductServiceImpl implements ISeckillProductService {
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ProductFeignApi productFeignApi;

    @Override
    public List<SeckillProductVo> selectByTime(int time) {
        //1查询秒杀商品集合数据
        List<SeckillProduct> seckillProducts = seckillProductMapper.queryCurrentlySeckillProduct(time);
        //2把商品id 放到list集合中
        ArrayList<Long> ids = new ArrayList<>();
        for (SeckillProduct seckillProduct : seckillProducts) {
            ids.add(seckillProduct.getProductId());
        }
        //3远程调用商品服务 把商品集合查询出来
        Result<List<Product>> result = productFeignApi.queryByIds(ids);
        ArrayList<SeckillProductVo> vos = new ArrayList<>();
        if (result!=null&&!result.hasError()){
            //4进行聚合操作最终返回List<SeckillProductVo>
            List<Product> products = result.getData();
            HashMap<Long, Product> map = new HashMap<>();
            for (Product product : products) {
                map.put(product.getId(),product);
            }
            for (SeckillProduct seckillProduct : seckillProducts) {
                SeckillProductVo seckillProductVo=new SeckillProductVo();
                Product product = map.get(seckillProduct.getProductId());
                BeanUtils.copyProperties(product,seckillProductVo);
                BeanUtils.copyProperties(seckillProduct,seckillProductVo);
                vos.add(seckillProductVo);
            }
        }
        return vos;
    }

    @Override
    public List<SeckillProductVo> queryByTime(int time) {
          List<Object> list = redisTemplate.opsForHash().values(SeckillRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time + ""));
        ArrayList<SeckillProductVo> vos = new ArrayList<>();
        for (Object objJson : list) {
            vos.add(JSON.parseObject(objJson+"",SeckillProductVo.class));
        }
        return vos;
    }

    @Override
    public SeckillProductVo find(int time, Long seckillId) {
        Object objJson = redisTemplate.opsForHash().get(SeckillRedisKey.SECKILL_PRODUCT_LIST.getRealKey(time + ""), seckillId + "");
        if (StringUtils.isEmpty(objJson+"")) {
            return null;
        }
        return JSON.parseObject(objJson+"",SeckillProductVo.class);
    }
}

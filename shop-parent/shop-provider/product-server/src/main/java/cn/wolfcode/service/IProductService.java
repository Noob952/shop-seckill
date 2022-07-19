package cn.wolfcode.service;

import cn.wolfcode.domain.Product;

import java.util.List;


public interface IProductService {
    /**
     * 根据商品id集合把商品查询出来
     * @param ids 商品id
     * @return
     */
    List<Product> queryByIds(List<Long> ids);
}

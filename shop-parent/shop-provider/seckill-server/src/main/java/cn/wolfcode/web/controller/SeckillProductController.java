package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.service.ISeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/seckillProduct")
@Slf4j
public class SeckillProductController {
    @Autowired
    private ISeckillProductService seckillProductService;

    @RequestMapping("/selectByTime")
     public Result<List<SeckillProductVo>> selectByTime(@RequestParam("time")int time) {
        return Result.success(seckillProductService.selectByTime(time));
    }

    @RequestMapping("/queryByTime")
     public Result<List<SeckillProductVo>> queryByTime(int time) {
        return Result.success(seckillProductService.queryByTime(time));
    }
    @RequestMapping("/find")
    public Result<SeckillProductVo> find(int time,Long seckillId){
        return Result.success(seckillProductService.find(time,seckillId));
    }
}

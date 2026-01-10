package com.alex.mallseckill.controller;

import com.alex.common.utils.R;
import com.alex.mallseckill.service.SeckillService;
import com.alex.mallseckill.to.SecKillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SeckillController {
    @Autowired
    private SeckillService seckillService;
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus(){
        List<SecKillSkuRedisTO> res = seckillService.getCurrentSeckillSkus();

        return R.ok().setData(res);
    }

    @GetMapping("/sku/seckill/<skuId>")
    public R getSkuSeckillInfo(Long skuId){
        SecKillSkuRedisTO res = seckillService.getSkuSeckillInfo(skuId);

        return R.ok().setData(res);
    }

    @GetMapping("/secKill")
    public R secKill(@RequestParam("killId") String killId,
                     @RequestParam("key") String key,
                     @RequestParam("num") Integer num,
                     @RequestParam("userId") Long userId) throws InterruptedException {


        String orderSn = seckillService.secKill(killId, key, num, userId);
        return R.ok().setData(orderSn);
    }

}

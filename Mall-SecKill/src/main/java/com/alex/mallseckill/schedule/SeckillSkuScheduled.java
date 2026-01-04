package com.alex.mallseckill.schedule;

import com.alex.mallseckill.feign.CouponFeignService;
import com.alex.mallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    private SeckillService seckillService;
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days(){
        log.info("upload seckill sku info for the next 3 days");
        //ignore the duplicate items
        seckillService.uploadSeckillSkuLatest3Days();
    }
}

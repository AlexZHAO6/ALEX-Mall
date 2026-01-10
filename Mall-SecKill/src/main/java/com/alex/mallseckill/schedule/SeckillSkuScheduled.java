package com.alex.mallseckill.schedule;

import com.alex.mallseckill.feign.CouponFeignService;
import com.alex.mallseckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    private SeckillService seckillService;
    @Autowired
    private RedissonClient redissonClient;
    private static final String upload_lock="seckill:upload:lock";
    @Scheduled(cron = "0 0 3 * * ?")
    //TODO add ttl for each item
    public void uploadSeckillSkuLatest3Days(){
        log.info("upload seckill sku info for the next 3 days");
        //ignore the duplicate items
        //idempotency
        //add distributed lock
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(20, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }
        finally {
            lock.unlock();
        }
    }
}

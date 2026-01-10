package com.alex.mallseckill.service;

import com.alex.mallseckill.to.SecKillSkuRedisTO;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTO> getCurrentSeckillSkus();

    SecKillSkuRedisTO getSkuSeckillInfo(Long skuId);

    String secKill(String killId, String key, Integer num, Long userId) throws InterruptedException;
}

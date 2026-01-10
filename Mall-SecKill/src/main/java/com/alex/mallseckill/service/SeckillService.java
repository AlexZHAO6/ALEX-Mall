package com.alex.mallseckill.service;

import com.alex.mallseckill.to.SecKillSkuRedisTO;

import java.util.List;

public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTO> getCurrentSeckillSkus();
}

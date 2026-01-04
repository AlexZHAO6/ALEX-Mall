package com.alex.mallseckill.service.impl;

import com.alex.common.utils.R;
import com.alex.mallseckill.feign.CouponFeignService;
import com.alex.mallseckill.feign.ProductFeignService;
import com.alex.mallseckill.service.SeckillService;
import com.alex.mallseckill.to.SecKillSkuRedisTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vo.SeckillSessionWithSkus;
import vo.SeckillSkuVO;
import vo.SkuInfoVO;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RedissonClient redissonClient;
    private static final String SESSION_CANCHE_PREFIX = "seckill:sessions:";
    private static final String SKU_CANCHE_PREFIX = "seckill:skus:";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:";
    @Override
    public void uploadSeckillSkuLatest3Days() {
        R session = couponFeignService.getLatest3DaysSession();
        if(session.getCode() == 0){
            List<SeckillSessionWithSkus> data = session.getData(new TypeReference<List<SeckillSessionWithSkus>>() {});

            //set into Redis
            //campaign info, campaign items info
            saveSessionInfos(data);
            saveSessionSkuInfos(data);
        }
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> data){
        data.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();

            String key = SESSION_CANCHE_PREFIX + startTime + "_" + endTime;

            if(!stringRedisTemplate.hasKey(key)){
                List<String> collected = session.getRelationEntities().stream().map(item->item.getPromotionSessionId().toString()+"_"+item.getSkuId().toString()).collect(Collectors.toList());
                stringRedisTemplate.opsForList().leftPushAll(key, collected);
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> data){
        data.stream().forEach(session -> {
            BoundHashOperations<String, Object, Object> boundHashOperations = stringRedisTemplate.boundHashOps(SKU_CANCHE_PREFIX);
            session.getRelationEntities().forEach(item -> {
                SecKillSkuRedisTO redisTO = new SecKillSkuRedisTO();
                Long skuId = item.getSkuId();
                String random = UUID.randomUUID().toString().replace("-", "");

                if(!boundHashOperations.hasKey(item.getPromotionSessionId().toString()+"_"+skuId.toString())){
                    //1. sku info
                    R info = productFeignService.info(skuId);
                    if(info.getCode() == 0){
                        SkuInfoVO skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVO>() {});
                        redisTO.setSkuInfo(skuInfo);
                    }
                    //2. sku seckill info
                    BeanUtils.copyProperties(item, redisTO);
                    //3. random num, start time, end time
                    //random num for avoiding attack
                    redisTO.setStartTime(session.getStartTime().getTime());
                    redisTO.setEndTime(session.getEndTime().getTime());

                    redisTO.setRandomCode(random);

                    String jsonString = JSON.toJSONString(redisTO);
                    boundHashOperations.put(item.getPromotionSessionId().toString()+"_"+item.getSkuId().toString(), jsonString);

                    //4. distributed semaphore -- for limiting throughput
                    //the semaphore is the stock value, if the semaphore <= 0, no need accessing DB
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + random);
                    semaphore.trySetPermits(redisTO.getSeckillCount().intValue());
                }
            });
        });

    }
}

package com.alex.mallseckill.service.impl;

import com.alex.common.to.SeckillOrderTO;
import com.alex.common.utils.R;
import com.alex.mallseckill.feign.CouponFeignService;
import com.alex.mallseckill.feign.ProductFeignService;
import com.alex.mallseckill.service.SeckillService;
import com.alex.mallseckill.to.SecKillSkuRedisTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import vo.SeckillSessionWithSkus;
import vo.SeckillSkuVO;
import vo.SkuInfoVO;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private RabbitTemplate rabbitTemplate;
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


    @Override
    public List<SecKillSkuRedisTO> getCurrentSeckillSkus() {
        Long current = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSION_CANCHE_PREFIX + "*");

        for (String key : keys){
            String replace = key.replace(SESSION_CANCHE_PREFIX, "");
            String[] times = replace.split("_");

            Long start = Long.parseLong(times[0]);
            Long end = Long.parseLong(times[1]);

            if(current >= start && current <= end){
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKU_CANCHE_PREFIX);

                List<String> res = boundHashOperations.multiGet(range);
                if(res != null){
                    List<SecKillSkuRedisTO> collect = res.stream().map(item -> {
                        SecKillSkuRedisTO secKillSkuRedisTO = new SecKillSkuRedisTO();
                        SecKillSkuRedisTO redis = com.alibaba.fastjson2.JSON.parseObject(item.toString(), secKillSkuRedisTO.getClass());

                        //redis.setRandomCode(null);

                        return redis;
                    }).collect(Collectors.toList());

                    return collect;
                }
            }
        }

        return null;
    }

    @Override
    public SecKillSkuRedisTO getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKU_CANCHE_PREFIX);
        Set<String> keys = boundHashOperations.keys();
        for (String key : keys){
            String[] split = key.split("_");
            if(skuId.toString().equals(split[1])){
                String json = boundHashOperations.get(key);
                SecKillSkuRedisTO secKillSkuRedisTO = new SecKillSkuRedisTO();
                SecKillSkuRedisTO redis = com.alibaba.fastjson2.JSON.parseObject(json, secKillSkuRedisTO.getClass());

                Long current = new Date().getTime();
                if(current < redis.getStartTime() || current > redis.getEndTime()){
                    redis.setRandomCode(null);
                }

                return redis;
            }
        }

        return null;
    }

    @Override
    public String secKill(String killId, String key, Integer num, Long userId) throws InterruptedException {
        //1. verification
        BoundHashOperations<String, String, String> boundHashOperations = stringRedisTemplate.boundHashOps(SKU_CANCHE_PREFIX);
        String s = boundHashOperations.get(killId);
        if(StringUtils.isEmpty(s)) {
            return null;
        }

        SecKillSkuRedisTO secKillSkuRedisTO = com.alibaba.fastjson2.JSON.parseObject(s, SecKillSkuRedisTO.class);
        Long current = new Date().getTime();
        if(current < secKillSkuRedisTO.getStartTime() || current > secKillSkuRedisTO.getEndTime()){
            return null;
        }
        if(!secKillSkuRedisTO.getRandomCode().equals(key)){
            return null;
        }
        if(num > secKillSkuRedisTO.getSeckillLimit().intValue()){
            return null;
        }
        //user can only buy once --- idempotency
        String redisKey = userId.toString()+"_"+killId;
        Long ttl = secKillSkuRedisTO.getEndTime() - current;
        Boolean isSet = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);

        if(!isSet){
            //already bought
            return null;
        }

        //2. semaphore -- extract stock
        RSemaphore rSemaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + secKillSkuRedisTO.getRandomCode());
        boolean acquired = rSemaphore.tryAcquire(num);
        if (!acquired){
            return null;
        }
        //3. send order to MQ
        String orderSn = UUID.randomUUID().toString().replace("-", "");
        SeckillOrderTO seckillOrderTO = new SeckillOrderTO();
        seckillOrderTO.setOrderSn(orderSn);
        seckillOrderTO.setSkuId(secKillSkuRedisTO.getSkuId());
        seckillOrderTO.setPromotionSessionId(secKillSkuRedisTO.getPromotionSessionId());
        seckillOrderTO.setSeckillPrice(secKillSkuRedisTO.getSeckillPrice());
        seckillOrderTO.setNum(num);
        seckillOrderTO.setMemberId(userId);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTO);

        return orderSn;
    }
}

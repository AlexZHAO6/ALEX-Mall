package com.alex.mallseckill.to;

import lombok.Data;
import vo.SkuInfoVO;

import java.math.BigDecimal;

@Data
public class SecKillSkuRedisTO {
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private BigDecimal seckillCount;
    /**
     * 每人限购数量
     */
    private BigDecimal seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //add sku infos
    private SkuInfoVO skuInfo;

    private Long startTime;

    private Long endTime;

    private String randomCode;
}

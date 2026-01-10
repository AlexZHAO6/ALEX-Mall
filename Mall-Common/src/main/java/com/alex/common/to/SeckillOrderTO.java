package com.alex.common.to;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTO {
    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Long memberId;
    private Integer num;
}

package com.alex.mallware.vo;

import lombok.Data;

@Data
public class LockStockResultVO {
    private String orderSn;
    private Boolean lock;
    private Long skuId;
    private Integer num;
}

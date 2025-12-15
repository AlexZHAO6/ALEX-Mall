package com.alex.mallorder.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVO {
    private Long userId;
    private Long addrId;
    private Integer payType;
    private String orderToken;
    private BigDecimal payPrice;
    private String note;
}

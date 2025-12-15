package com.alex.mallware.vo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class ShippingResponseVO {
    private MemberAddressVO address;
    private BigDecimal shippingFee;
}

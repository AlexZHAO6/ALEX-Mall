package com.alex.mallorder.vo;

import com.alex.mallorder.entity.OrderEntity;
import com.alex.mallorder.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTO {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal shippingFee;
}

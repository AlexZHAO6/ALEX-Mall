package com.alex.mallorder.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareLockVO {
    private String orderSn;
    private List<OrderItemVO> locks;
}

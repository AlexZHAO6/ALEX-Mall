package com.alex.mallorder.vo;

import com.alex.mallorder.entity.OrderEntity;
import lombok.Data;

@Data
public class SubmitOrderResponseVO {
    private OrderEntity order;
    // status code, 0: success, other fail
    private Integer code;
}

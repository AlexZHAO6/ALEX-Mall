package com.alex.mallorder.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallorder.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:20:48
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


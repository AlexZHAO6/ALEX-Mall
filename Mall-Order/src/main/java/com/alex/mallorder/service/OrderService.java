package com.alex.mallorder.service;

import com.alex.mallorder.vo.OrderConfirmVO;
import com.alex.mallorder.vo.OrderSubmitVO;
import com.alex.mallorder.vo.SubmitOrderResponseVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallorder.entity.OrderEntity;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:20:48
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder(Long userId) throws ExecutionException, InterruptedException;

    SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO);

    OrderEntity getOrderByOrderSn(String orderSn);
}


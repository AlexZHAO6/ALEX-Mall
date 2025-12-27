package com.alex.mallorder.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallorder.vo.OrderConfirmVO;
import com.alex.mallorder.vo.OrderSubmitVO;
import com.alex.mallorder.vo.SubmitOrderResponseVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallorder.entity.OrderEntity;
import com.alex.mallorder.service.OrderService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * 订单
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:20:48
 */
@RestController
@RequestMapping("mallorder/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallorder:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mallorder:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallorder:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallorder:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallorder:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/trade")
    public R getTrade(@RequestParam("userId") Long userId) throws ExecutionException, InterruptedException {
       OrderConfirmVO res = orderService.confirmOrder(userId);

       return R.ok().put("trade", res);
    }

    @PostMapping("/submitOrder")
    public R submitOrder(@RequestBody OrderSubmitVO orderSubmitVO){
        SubmitOrderResponseVO res = orderService.submitOrder(orderSubmitVO);
        if(res.getCode() == 0){
            return R.ok().put("orderResponse", res);
        }
        else{
            return switch (res.getCode()) {
                case 1 -> R.error(400, "Order information expired, please refresh and resubmit");
                case 2 -> R.error(400, "Price verification failed, please confirm and resubmit");
                case 3 -> R.error(400, "Insufficient stock for one or more items");
                default -> R.error(400, "Unknown error");
            };
        }
    }

    @GetMapping("/orderInfo/{orderSn}")
    public R getOrderInfo(@PathVariable("orderSn") String orderSn){
        OrderEntity order = orderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(order);

    }

//    @GetMapping("/test/order")
//    public String testCreateOrder(){
//        OrderEntity entity = new OrderEntity();
//        entity.setOrderSn(UUID.randomUUID().toString());
//        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", entity);
//        return "ok";
//    }
}

package com.alex.mallorder.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallorder.vo.OrderConfirmVO;
import com.alex.mallorder.vo.OrderSubmitVO;
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
    public R submitOrder(@RequestBody OrderSubmitVO orderSubmitVO) throws ExecutionException, InterruptedException {


        return R.ok().put("order", null);
    }


}

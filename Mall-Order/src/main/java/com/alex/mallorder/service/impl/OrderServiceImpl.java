package com.alex.mallorder.service.impl;

import com.alex.common.utils.R;
import com.alex.mallorder.feign.CartFeignService;
import com.alex.mallorder.feign.MemberFeignService;
import com.alex.mallorder.feign.WareFeignService;
import com.alex.mallorder.vo.MemberAddressVO;
import com.alex.mallorder.vo.OrderConfirmVO;
import com.alex.mallorder.vo.OrderItemVO;
import com.alex.mallorder.vo.SkuStockVO;
import com.alibaba.fastjson2.TypeReference;
import constant.OrderConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallorder.dao.OrderDao;
import com.alex.mallorder.entity.OrderEntity;
import com.alex.mallorder.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder(Long userId) throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();

        //1. call member service, async get user address
        CompletableFuture<Void> getAddress = CompletableFuture.runAsync(() -> {

            List<MemberAddressVO> address = memberFeignService.getAddress(userId);
            orderConfirmVO.setAddress(address);
        }, executor);

        //2. call cart service, async get current user cart items
        CompletableFuture<Void> getCartItems = CompletableFuture.runAsync(() -> {

            List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems(userId);
            orderConfirmVO.setItems(currentUserCartItems);
        }, executor);


        //3. get ware stock information, async
        CompletableFuture<Void> getStock = getCartItems.thenRunAsync(() -> {
            List<OrderItemVO> items = orderConfirmVO.getItems();
            if (items != null && items.size() > 0) {
                List<Long> itemIds = items.stream().map(OrderItemVO::getSkuId).toList();
                R skuHasStock = wareFeignService.getSkuHasStock(itemIds);
                List<SkuStockVO> data = skuHasStock.getData(new TypeReference<List<SkuStockVO>>() {
                });
                if (data != null) {
                    Map<Long, Boolean> stockMap = data.stream().
                            collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));

                    orderConfirmVO.setStocks(stockMap);
                }
            }
        }, executor);

        //4. check member points.
        //TODO call member service to get points
        orderConfirmVO.setMemberPoints(1000);

        //5. calculate total amount and pay amount
        orderConfirmVO.getTotalAmount();
        orderConfirmVO.getPayAmount();

        //6. generate order token to prevent duplicate submissions -- idempotency
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVO.setOrderToken(orderToken);
        stringRedisTemplate.opsForValue().
                set(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId, orderToken,30, TimeUnit.MINUTES);
        //wait for the tasks to finish
        CompletableFuture.allOf(getAddress, getCartItems, getStock).get();
        return orderConfirmVO;
    }

}
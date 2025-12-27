package com.alex.mallorder.service.impl;

import com.alex.common.utils.R;
import com.alex.mallorder.dao.OrderItemDao;
import com.alex.mallorder.enume.OrderStatusEnum;
import com.alex.mallorder.feign.CartFeignService;
import com.alex.mallorder.feign.MemberFeignService;
import com.alex.mallorder.feign.ProductFeignService;
import com.alex.mallorder.feign.WareFeignService;
import com.alex.mallorder.service.OrderItemService;
import com.alex.mallorder.vo.*;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import constant.OrderConstant;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
import com.alex.mallorder.entity.OrderItemEntity;
import org.springframework.transaction.annotation.Transactional;


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
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private OrderItemService orderItemService;

    private ThreadLocal<OrderSubmitVO> submitVOThreadLocal = new ThreadLocal<>();

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

//    @GlobalTransactional --- too slow
    @Transactional
    @Override
    public SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO) {
        SubmitOrderResponseVO responseVO = new SubmitOrderResponseVO();
        responseVO.setCode(0);
        String orderToken = orderSubmitVO.getOrderToken();
        Long userId = orderSubmitVO.getUserId();
        submitVOThreadLocal.set(orderSubmitVO);

        //For making sure the command(compare and delete) is atomic, use lua script
        //o stands for fail, 1 stands for success
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                List.of(OrderConstant.USER_ORDER_TOKEN_PREFIX + userId), orderToken);

        if(execute == 0L){
            responseVO.setCode(1);
            return responseVO;
        }
        else {
            //create order, verify price, lock stock
            OrderCreateTO orderCreateTO = createOrder();
            //check the price
            BigDecimal payAmount = orderCreateTO.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVO.getPayPrice();

            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 1000000) {
                //price check passed
                //save the order into DB
                saveOrder(orderCreateTO);

                //lock stock
                WareLockVO wareLockVO = new WareLockVO();
                wareLockVO.setOrderSn(orderCreateTO.getOrder().getOrderSn());
                List<OrderItemVO> locks = orderCreateTO.getOrderItems().stream().map(item -> {
                    OrderItemVO orderItemVO = new OrderItemVO();
                    orderItemVO.setSkuId(item.getSkuId());
                    orderItemVO.setCount(item.getSkuQuantity());
                    orderItemVO.setTitle(item.getSkuName());
                    return orderItemVO;
                }).collect(Collectors.toList());
                wareLockVO.setLocks(locks);

                R lockRes = wareFeignService.orderLockStock(wareLockVO);
                if(lockRes.getCode() == 0){
                    //lock successfully

                    int i = 10/0; //test rollback
                    responseVO.setOrder(orderCreateTO.getOrder());
                    return responseVO;
                }
                else {
                    //lock failed
                    responseVO.setCode(3);
                    return responseVO;
                }
            }
            else {
                //price check failed
                responseVO.setCode(2);
                return responseVO;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    private void saveOrder(OrderCreateTO orderCreateTO) {
        OrderEntity orderEntity = orderCreateTO.getOrder();
        List<OrderItemEntity> orderItems = orderCreateTO.getOrderItems();

        orderEntity.setModifyTime(new Date());
        this.baseMapper.insert(orderEntity);

        for (OrderItemEntity item : orderItems) {
            item.setOrderId(orderEntity.getId());
        }
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTO createOrder(){
        OrderCreateTO orderCreateTO = new OrderCreateTO();
        OrderEntity orderEntity = buildOrder();
        String orderSn = orderEntity.getOrderSn();
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);

        //calculate total amount, etc.
        calculatePrice(orderEntity, orderItems);

        orderCreateTO.setOrder(orderEntity);
        orderCreateTO.setOrderItems(orderItems);
        orderCreateTO.setPayPrice(orderEntity.getPayAmount());
        orderCreateTO.setShippingFee(orderEntity.getFreightAmount());
        return orderCreateTO;
    }

    private void calculatePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal memberPoints = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");


        for (OrderItemEntity item : orderItems) {
            BigDecimal finalPrice = item.getRealAmount();
            coupon = coupon.add(item.getCouponAmount());
            promotion = promotion.add(item.getPromotionAmount());
            memberPoints = memberPoints.add(item.getIntegrationAmount());
            total = total.add(finalPrice);
            gift = gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth = growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }

        orderEntity.setTotalAmount(total);
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(memberPoints);
        orderEntity.setGrowth(growth.intValue());
        orderEntity.setIntegration(gift.intValue());
    }

    private OrderEntity buildOrder() {
        OrderEntity orderEntity = new OrderEntity();
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);


        OrderSubmitVO orderSubmitVO = submitVOThreadLocal.get();
        orderEntity.setMemberId(orderSubmitVO.getUserId());
        //set shipping related fields
        R res = wareFeignService.getShippingFee(orderSubmitVO.getAddrId());
        ShippingResponseVO shippingResponseVO = res.getData(new TypeReference<ShippingResponseVO>(){});
        orderEntity.setFreightAmount(shippingResponseVO.getShippingFee());
        orderEntity.setReceiverCity(shippingResponseVO.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(shippingResponseVO.getAddress().getDetailAddress());
        orderEntity.setReceiverName(shippingResponseVO.getAddress().getName());
        orderEntity.setReceiverPhone(shippingResponseVO.getAddress().getPhone());
        orderEntity.setReceiverPostCode(shippingResponseVO.getAddress().getPostCode());
        orderEntity.setReceiverProvince(shippingResponseVO.getAddress().getProvince());
        orderEntity.setReceiverRegion(shippingResponseVO.getAddress().getRegion());

        //set status
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        //status: 0, stands for not deleted
        orderEntity.setDeleteStatus(0);

        return orderEntity;
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn){
        OrderSubmitVO orderSubmitVO = submitVOThreadLocal.get();
        List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems(orderSubmitVO.getUserId());
        if(currentUserCartItems != null && currentUserCartItems.size() > 0){
            List<OrderItemEntity> orderItems = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                Long skuId = cartItem.getSkuId();
                orderItemEntity.setSkuId(skuId);
                orderItemEntity.setOrderSn(orderSn);
                orderItemEntity.setSkuQuantity(cartItem.getCount());
                orderItemEntity.setSkuPrice(cartItem.getPrice());
                orderItemEntity.setSkuName(cartItem.getTitle());
                orderItemEntity.setSkuPic(cartItem.getImage());
                orderItemEntity.setSkuAttrsVals(String.join(";", cartItem.getSkuAttr()));
                orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
                orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());

                //get spu info
                SpuInfoVO spuInfoVO = productFeignService.getSpuInfoBuSkuId(skuId).getData(new TypeReference<SpuInfoVO>(){});
                orderItemEntity.setSpuId(spuInfoVO.getId());
                orderItemEntity.setSpuName(spuInfoVO.getSpuName());
                orderItemEntity.setSpuBrand(spuInfoVO.getBrandId().toString());
                orderItemEntity.setCategoryId(spuInfoVO.getCatalogId());

                //set price related info
                orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
                orderItemEntity.setCouponAmount(new BigDecimal("0.0"));
                orderItemEntity.setIntegrationAmount(new BigDecimal("0.0"));
                BigDecimal originalPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
                BigDecimal finalAmount = originalPrice.subtract(orderItemEntity.getPromotionAmount())
                        .subtract(orderItemEntity.getCouponAmount())
                        .subtract(orderItemEntity.getIntegrationAmount());
                orderItemEntity.setRealAmount(finalAmount);

                return orderItemEntity;
            }).collect(Collectors.toList());

            return orderItems;
        }

        return null;
    }

}
package com.alex.mallcart.service.impl;

import com.alex.common.utils.R;
import com.alex.mallcart.feign.ProductFeignService;
import com.alex.mallcart.service.CartService;
import com.alex.mallcart.vo.Cart;
import com.alex.mallcart.vo.CartItem;
import com.alex.mallcart.vo.SkuInfoVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    private final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(Long userId, Long skuId, Integer num) {
        String cartKey = CART_PREFIX + userId;

        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);

        String res = (String) boundHashOperations.get(skuId.toString());
        if(StringUtils.isEmpty(res)){
            //add new item into the cart
            CartItem cartItem = new CartItem();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R skuInfo = productFeignService.info(skuId);
                TypeReference<SkuInfoVo> typeReference = new TypeReference<>() {
                };
                SkuInfoVo skuInfoVo = skuInfo.getData("skuInfo", typeReference);

                cartItem.setSkuId(skuId);
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setPrice(skuInfoVo.getPrice());
            }, executor);

            CompletableFuture<Void> getSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, executor);

            CompletableFuture.allOf(getSkuInfo, getSaleAttrValues).join();

            String jsonString = JSON.toJSONString(cartItem);

            boundHashOperations.put(skuId.toString(), jsonString);

            return cartItem;
        }
        else {
            //update the item in the cart
            CartItem existingCartItem = JSON.parseObject(res, CartItem.class);
            existingCartItem.setCount(existingCartItem.getCount() + num);

            String jsonString = JSON.toJSONString(existingCartItem);
            boundHashOperations.put(skuId.toString(), jsonString);
            return existingCartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId, Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        String res = (String) boundHashOperations.get(skuId.toString());

        if(!StringUtils.isEmpty(res)){
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            return cartItem;
        }

        return null;
    }

    @Override
    public Cart getCart(Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = boundHashOperations.values();
        Cart cart = new Cart();

        if(values != null && values.size() > 0){
            List<CartItem> ls = new ArrayList<>();
            for (Object res : values) {
                String jsonString = (String) res;
                CartItem cartItem = JSON.parseObject(jsonString, CartItem.class);
                ls.add(cartItem);
            }
            cart.setItems(ls);
        }

        return cart;
    }

    @Override
    public void checkItem(Long skuId, Integer check, Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        String res = (String) boundHashOperations.get(skuId.toString());

        if(!StringUtils.isEmpty(res)){
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCheck(check == 1);

            String jsonString = JSON.toJSONString(cartItem);
            boundHashOperations.put(skuId.toString(), jsonString);
        }
    }

    @Override
    public void countItem(Long skuId, Integer num, Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        String res = (String) boundHashOperations.get(skuId.toString());

        if(!StringUtils.isEmpty(res)){
            CartItem cartItem = JSON.parseObject(res, CartItem.class);
            cartItem.setCount(num);

            String jsonString = JSON.toJSONString(cartItem);
            boundHashOperations.put(skuId.toString(), jsonString);
        }
    }

    @Override
    public void deleteItem(Long skuId, Long userId) {
        String cartKey = CART_PREFIX + userId;
        BoundHashOperations<String, Object, Object> boundHashOperations = redisTemplate.boundHashOps(cartKey);
        boundHashOperations.delete(skuId.toString());
    }
}

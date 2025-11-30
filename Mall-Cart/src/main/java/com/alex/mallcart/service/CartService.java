package com.alex.mallcart.service;

import com.alex.mallcart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long userId, Long skuId, Integer num);
}

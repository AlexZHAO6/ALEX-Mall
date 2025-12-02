package com.alex.mallcart.service;

import com.alex.mallcart.vo.Cart;
import com.alex.mallcart.vo.CartItem;

public interface CartService {
    CartItem addToCart(Long userId, Long skuId, Integer num);
    CartItem getCartItem(Long skuId, Long userId);

    Cart getCart(Long userId);

    void checkItem(Long skuId, Integer check, Long userId);

    void countItem(Long skuId, Integer num, Long userId);

    void deleteItem(Long skuId, Long userId);
}

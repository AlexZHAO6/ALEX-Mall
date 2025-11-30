package com.alex.mallcart.controler;

import com.alex.common.utils.R;
import com.alex.mallcart.service.CartService;
import com.alex.mallcart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;
    @GetMapping("/cart/{userId}")
    public String getCartInfo(@PathVariable("userId") Long userId) {

        return "ok";
    }

    @GetMapping("/addToCart")
    public R addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, @RequestParam("userId") Long userId) {
        CartItem cartItem = cartService.addToCart(userId, skuId, num);

        return R.ok().put("cartItem", cartItem);
    }
}

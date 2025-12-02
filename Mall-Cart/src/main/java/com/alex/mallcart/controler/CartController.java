package com.alex.mallcart.controler;

import com.alex.common.utils.R;
import com.alex.mallcart.service.CartService;
import com.alex.mallcart.vo.Cart;
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

    @GetMapping("/getCartItem")
    public R getCartItem(@RequestParam("skuId") Long skuId, @RequestParam("userId") Long userId) {
        CartItem cartItem = cartService.getCartItem(userId, skuId);

        return R.ok().put("cartItem", cartItem);
    }

    @GetMapping("/getCart")
    public R getCart(@RequestParam("skuId") Long skuId, @RequestParam("userId") Long userId) {
        Cart cart = cartService.getCart(userId);

        return R.ok().put("cart", cart);
    }

    @GetMapping("/checkItem")
    public R checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check, @RequestParam("userId") Long userId) {
        cartService.checkItem(skuId, check, userId);

        return R.ok();
    }

    @GetMapping("/countItem")
    public R countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, @RequestParam("userId") Long userId) {
        cartService.countItem(skuId, num, userId);

        return R.ok();
    }

    @GetMapping("/deleteItem")
    public R deleteItem(@RequestParam("skuId") Long skuId, @RequestParam("userId") Long userId) {
        cartService.deleteItem(skuId, userId);

        return R.ok();
    }


}

package com.alex.mallproduct.web;

import com.alex.mallproduct.service.SkuInfoService;
import com.alex.mallproduct.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo vo = skuInfoService.item(skuId);
        return "item";
    }
}

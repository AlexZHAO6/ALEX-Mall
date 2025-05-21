package com.alex.mallsearch.controller;

import com.alex.common.to.es.SkuEsModel;
import com.alex.common.utils.R;
import com.alex.mallsearch.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> models){
        boolean b = false;
        try {
             b = productSaveService.productStatusUp(models);
        }
        catch (Exception e){
            log.error("onboard es failed", e.getMessage());
            return R.error("onboard es failed");
        }
        if(b){
            return R.ok();
        }
        else {
            return R.error("onboard es failed");
        }

    }
}

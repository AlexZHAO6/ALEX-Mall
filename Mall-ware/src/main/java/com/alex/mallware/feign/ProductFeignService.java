package com.alex.mallware.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("Mall-Product")
public interface ProductFeignService {

    @RequestMapping("/mallproduct/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}

package com.alex.mallorder.feign;

import com.alex.common.utils.R;
import com.alex.mallorder.feign.fallback.ProductFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "Mall-Product", fallback = ProductFeignServiceFallback.class)
public interface ProductFeignService {
    @GetMapping("/mallproduct/spuinfo/skuId/{id}")
    R getSpuInfoBuSkuId(@PathVariable("id") Long skuId);
}

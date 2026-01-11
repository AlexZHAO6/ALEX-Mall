package com.alex.mallorder.feign.fallback;

import com.alex.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductFeignServiceFallback implements com.alex.mallorder.feign.ProductFeignService{
    @Override
    public R getSpuInfoBuSkuId(Long skuId) {
        log.info("circuit breaker, ProductFeignServiceFallback: getSpuInfoBuSkuId fallback for skuId " + skuId);
        return R.error("Too many requests");
    }
}

package com.alex.mallproduct.feign;

import com.alex.common.to.SkuHasStockVO;
import com.alex.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("Mall-ware")
public interface WareFeignService {
    @PostMapping("/mallware/waresku/hasstock")
    R<List<SkuHasStockVO>> getSkuHasStock(@RequestBody List<Long> skuIds);
}

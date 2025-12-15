package com.alex.mallorder.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("Mall-Ware")
public interface WareFeignService {
    @PostMapping("/mallware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
}

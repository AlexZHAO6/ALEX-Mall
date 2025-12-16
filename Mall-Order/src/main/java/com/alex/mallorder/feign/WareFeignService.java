package com.alex.mallorder.feign;

import com.alex.common.utils.R;
import com.alex.mallorder.vo.WareLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("Mall-Ware")
public interface WareFeignService {
    @PostMapping("/mallware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
    @GetMapping("/mallware/wareinfo/shippingFee")
    R getShippingFee(@RequestParam("addrId") Long addrId);
    @PostMapping("/mallware/waresku/lock/order")
    R orderLockStock(@RequestBody WareLockVO vo);
}

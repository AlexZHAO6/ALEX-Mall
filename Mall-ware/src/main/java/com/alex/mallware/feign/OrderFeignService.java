package com.alex.mallware.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("Mall-Order")
public interface OrderFeignService {
    @GetMapping("/mallorder/order/orderInfo/{orderSn}")
    R getOrderInfo(@PathVariable("orderSn") String orderSn);
}

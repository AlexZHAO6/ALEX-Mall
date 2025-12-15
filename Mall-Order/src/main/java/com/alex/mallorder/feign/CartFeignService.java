package com.alex.mallorder.feign;

import com.alex.mallorder.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("Mall-Cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVO> getCurrentUserCartItems(@RequestParam("userId") Long userId);
}

package com.alex.mallseckill.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("Mall-Coupon")
public interface CouponFeignService {
    @GetMapping("/mallcoupon/seckillsession/latest3DaysSession")
    R getLatest3DaysSession();

}

package com.alex.mallmember.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("Mall-Coupon")
public interface CouponFeignService {
    @RequestMapping("/mallcoupon/coupon/member/list")
    public R memberCoupon();
}

package com.alex.mallware.feign;

import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("Mall-Member")
public interface MemberFeignService {
    @RequestMapping("/mallmember/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);
}

package com.alex.mallorder.feign;

import com.alex.mallorder.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("Mall-Member")
public interface MemberFeignService {
    @GetMapping("/mallmember/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVO> getAddress(@PathVariable("memberId") Long memberId);
}

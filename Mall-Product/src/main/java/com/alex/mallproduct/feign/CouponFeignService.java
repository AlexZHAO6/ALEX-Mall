package com.alex.mallproduct.feign;

import com.alex.common.to.SkuReductionTO;
import com.alex.common.to.SpuBoundsTO;
import com.alex.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("Mall-Coupon")
public interface CouponFeignService {

    //远程调用会将参数转为JSON, 到达远程服务后再把JSON转回为Object
    //所以远程接口与对方服务参数类型可以不一样 属性名要一一对应 e.g: SpuBoundsTO, SpuBoundsEntity
    @PostMapping("/mallcoupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTO spuBoundsTO);

    @PostMapping("mallcoupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTO skuReductionTO);
}

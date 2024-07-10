package com.alex.mallcoupon.dao;

import com.alex.mallcoupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 21:37:31
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

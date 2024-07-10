package com.alex.mallorder.dao;

import com.alex.mallorder.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:20:48
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

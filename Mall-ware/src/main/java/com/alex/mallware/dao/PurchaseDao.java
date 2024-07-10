package com.alex.mallware.dao;

import com.alex.mallware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:29:18
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}

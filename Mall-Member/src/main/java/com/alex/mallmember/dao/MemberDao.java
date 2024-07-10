package com.alex.mallmember.dao;

import com.alex.mallmember.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 10:34:44
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

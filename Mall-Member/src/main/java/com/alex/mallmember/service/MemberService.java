package com.alex.mallmember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallmember.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 10:34:44
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


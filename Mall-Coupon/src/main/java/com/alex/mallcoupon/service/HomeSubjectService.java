package com.alex.mallcoupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallcoupon.entity.HomeSubjectEntity;

import java.util.Map;

/**
 * 首页专题表【jd首页下面很多专题，每个专题链接新的页面，展示专题商品信息】
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 21:37:31
 */
public interface HomeSubjectService extends IService<HomeSubjectEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


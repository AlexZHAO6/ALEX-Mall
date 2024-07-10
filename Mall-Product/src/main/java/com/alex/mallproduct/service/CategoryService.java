package com.alex.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallproduct.entity.CategoryEntity;

import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


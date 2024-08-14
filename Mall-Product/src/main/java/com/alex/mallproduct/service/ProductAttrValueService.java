package com.alex.mallproduct.service;

import com.alex.mallproduct.vo.BaseAttrs;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallproduct.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBaseAttrs(Long id, List<BaseAttrs> baseAttrs);
}


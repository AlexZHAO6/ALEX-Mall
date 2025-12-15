package com.alex.mallware.service;

import com.alex.mallware.vo.ShippingResponseVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallware.entity.WareInfoEntity;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 仓库信息
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:29:18
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    ShippingResponseVO getShippingFee(Long addrId);
}


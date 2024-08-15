package com.alex.mallware.service;

import com.alex.mallware.vo.MergeVO;
import com.alex.mallware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alex.common.utils.PageUtils;
import com.alex.mallware.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:29:18
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivedPurchase(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void finishPurchase(PurchaseDoneVO vo);
}


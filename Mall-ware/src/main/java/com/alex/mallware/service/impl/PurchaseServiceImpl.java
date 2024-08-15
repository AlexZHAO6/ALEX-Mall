package com.alex.mallware.service.impl;

import com.alex.common.constant.WareConstant;
import com.alex.mallware.entity.PurchaseDetailEntity;
import com.alex.mallware.entity.WareSkuEntity;
import com.alex.mallware.service.PurchaseDetailService;
import com.alex.mallware.service.WareSkuService;
import com.alex.mallware.vo.ItemDoneVO;
import com.alex.mallware.vo.MergeVO;
import com.alex.mallware.vo.PurchaseDoneVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallware.dao.PurchaseDao;
import com.alex.mallware.entity.PurchaseEntity;
import com.alex.mallware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivedPurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if(purchaseId == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();

        }

        List<Long> items = mergeVO.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> detailEntityList = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(detailEntityList);
    }

    @Override
    @Transactional
    public void finishPurchase(PurchaseDoneVO vo) {
        
        //1. change purchase detail info
        Boolean flag = true;
        List<ItemDoneVO> items = vo.getItems();
        List<PurchaseDetailEntity> update = new ArrayList<>();
        for(ItemDoneVO item: items){
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ERROR.getCode()){
                flag = false;
            }
            else {
                //2. change those successful purchase actions
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());

                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }

            purchaseDetailEntity.setStatus(item.getStatus());
            purchaseDetailEntity.setId(item.getItemId());
            update.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(update);

        //3. change purchase info
        Long purchaseId = vo.getPurchaseId();
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISHED.getCode() : WareConstant.PurchaseStatusEnum.ERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}
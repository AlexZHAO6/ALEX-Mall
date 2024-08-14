package com.alex.mallcoupon.service.impl;

import com.alex.common.to.MemberPrice;
import com.alex.common.to.SkuReductionTO;
import com.alex.mallcoupon.entity.MemberPriceEntity;
import com.alex.mallcoupon.entity.SkuLadderEntity;
import com.alex.mallcoupon.service.MemberPriceService;
import com.alex.mallcoupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallcoupon.dao.SkuFullReductionDao;
import com.alex.mallcoupon.entity.SkuFullReductionEntity;
import com.alex.mallcoupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;
    @Autowired
    private MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTO skuFullReduction) {
        //sku member and price related info   sms_sku_ladder/sms_sku_full_reduction/sms_member_price
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuFullReduction.getSkuId());
        skuLadderEntity.setFullCount(skuFullReduction.getFullCount());
        skuLadderEntity.setDiscount(skuFullReduction.getDiscount());
        skuLadderEntity.setAddOther(skuFullReduction.getCountStatus());
        skuLadderService.save(skuLadderEntity);

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuFullReduction, skuFullReductionEntity);
        this.save(skuFullReductionEntity);

        List<MemberPrice> memberPrices = skuFullReduction.getMemberPrice();
        if(memberPrices != null && memberPrices.size() > 0){
            List<MemberPriceEntity> memberPriceEntities = memberPrices.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuFullReduction.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(itm -> itm.getMemberPrice().compareTo(new BigDecimal(0)) == 1).collect(Collectors.toList());

            memberPriceService.saveBatch(memberPriceEntities);
        }

    }

}
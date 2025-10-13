package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.entity.SkuImagesEntity;
import com.alex.mallproduct.entity.SpuInfoDescEntity;
import com.alex.mallproduct.service.*;
import com.alex.mallproduct.vo.SkuItemVo;
import com.alex.mallproduct.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallproduct.dao.SkuInfoDao;
import com.alex.mallproduct.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private AttrGroupService attrGroupService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> entities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return entities;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();
        //1. basic sku info -- pms_sku_info
        SkuInfoEntity skuInfoEntity = getById(skuId);
        skuItemVo.setSkuInfoEntity(skuInfoEntity);
        //2. sku image info -- pms_sku_images
        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
        skuItemVo.setImages(images);
        //3. sku's spu related info --


        //4. sku's spu introduction info
        Long spuId = skuInfoEntity.getSpuId();
        SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(spuId);
        skuItemVo.setSpuInfoDescEntity(spuInfoDesc);
        //5. sku's spu attrs
        Long catalogId = skuInfoEntity.getCatalogId();
        List<SpuItemAttrGroupVo> attrs = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return skuItemVo;
    }

}
package com.alex.mallproduct.vo;

import com.alex.mallproduct.entity.SkuImagesEntity;
import com.alex.mallproduct.entity.SkuInfoEntity;
import com.alex.mallproduct.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    private SkuInfoEntity skuInfoEntity;
    private List<SkuImagesEntity> images;
    private List<SkuItemSaleAttrVo> saleAttrs;
    private SpuInfoDescEntity spuInfoDescEntity;
    private List<SpuItemAttrGroupVo> groupAttrs;
}

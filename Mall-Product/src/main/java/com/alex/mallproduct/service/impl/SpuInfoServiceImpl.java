package com.alex.mallproduct.service.impl;

import com.alex.common.constant.ProductConstant;
import com.alex.common.to.SkuHasStockVO;
import com.alex.common.to.SkuReductionTO;
import com.alex.common.to.SpuBoundsTO;
import com.alex.common.to.es.SkuEsModel;
import com.alex.common.utils.R;
import com.alex.mallproduct.entity.*;
import com.alex.mallproduct.feign.CouponFeignService;
import com.alex.mallproduct.feign.SearchFeignService;
import com.alex.mallproduct.feign.WareFeignService;
import com.alex.mallproduct.service.*;
import com.alex.mallproduct.vo.*;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallproduct.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuInfo) {
        //TODO: deal with Distributed Transaction
        //1. save basic info    pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2. save spu description info  pms_spu_info_desc
        List<String> desc = spuInfo.getDecript();
        spuInfoDescService.saveDesc(spuInfoEntity.getId(), desc);


        //3. save spu images info   pms_spu_images
        List<String> images = spuInfo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4. save spu attrs info    pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuInfo.getBaseAttrs();
        productAttrValueService.saveBaseAttrs(spuInfoEntity.getId(), baseAttrs);

        //5. save spu points info   sms_spu_bounds
        Bounds bounds = spuInfo.getBounds();
        SpuBoundsTO spuBoundsTO = new SpuBoundsTO();
        BeanUtils.copyProperties(bounds, spuBoundsTO);
        spuBoundsTO.setSpuId(spuInfoEntity.getId());

        R r = couponFeignService.saveSpuBounds(spuBoundsTO);
        if(r.getCode() != 0){
            log.error("remote couponFeignService saveSpuBounds failed");
        }
        //6. save spu's sku info
        List<Skus> skus = spuInfo.getSkus();
        if(skus != null && skus.size() > 0){
            skus.forEach(item -> {
                //6.1 sku basic info    pms_sku_info
                String defaultImg = "";
                for(Images image: item.getImages()){
                    if(image.getDefaultImg() == 1)
                        defaultImg = image.getImgUrl();
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.save(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //6.2 sku images info   pms_sku_images
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                //6.3 sku attr info     pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                //6.4 sku member and price related info   sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTO skuReductionTO = new SkuReductionTO();
                BeanUtils.copyProperties(item, skuReductionTO);
                skuReductionTO.setSkuId(skuId);
                if(skuReductionTO.getFullCount() > 0 || skuReductionTO.getFullPrice().compareTo(new BigDecimal(0)) > 0){
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTO);
                    if(r1.getCode() != 0){
                        log.error("remote couponFeignService saveSkuReduction failed");
                    }
                }

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> spuInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            spuInfoEntityQueryWrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            spuInfoEntityQueryWrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)){
            spuInfoEntityQueryWrapper.eq("brand_id", brandId);
        }
        String catalogId = (String) params.get("catalogId");
        if(!StringUtils.isEmpty(catalogId)){
            spuInfoEntityQueryWrapper.eq("catalog_id", catalogId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                spuInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        //get all searchable attrs by spuId!
        List<ProductAttrValueEntity> attrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> arrrIds = attrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());


        List<Long> searchableIds = attrService.selectSearchAttrs(arrrIds);
        Set<Long> ids = new HashSet<>(searchableIds);
        List<SkuEsModel.Attrs> skuAttrs = attrs.stream().filter(attr -> ids.contains(attr.getAttrId())).map(attr -> {
            SkuEsModel.Attrs attr1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(attr, attr1);
            return attr1;
        }).toList();

        //call remote ware service to check if skus has stock
        Map<Long, Boolean> hasStockMap = null;
        try {
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).toList();
            R res = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockVO>> typeReference = new TypeReference<>(){
            };
            hasStockMap = res.getData(typeReference).stream().collect(Collectors.
                    toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
        }
        catch (Exception e) {
            log.error("call remote ware service error", e.getCause());
        }


        Map<Long, Boolean> finalHasStockMap = hasStockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImage(sku.getSkuDefaultImg());
            //set up stock info
            if(finalHasStockMap == null){
                esModel.setHasStock(false);
            }
            else {
                esModel.setHasStock(finalHasStockMap.get(sku.getSkuId()));
            }

            //TODO: hotScore default -- 0 (can add more logic)
            esModel.setHotScore(0L);
            BrandEntity brandEntity = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            //set up skus searchable attrs
            esModel.setAttrs(skuAttrs);


            return esModel;
        }).collect(Collectors.toList());

        //send the data to ES
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //change the status of SPU
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }
        else {
            //TODO: deal with onboard error
            //TODO：Interface idempotent？retry？
        }

    }
}
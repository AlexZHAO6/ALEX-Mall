package com.alex.mallware.service.impl;

import com.alex.common.utils.R;
import com.alex.mallware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallware.dao.WareSkuDao;
import com.alex.mallware.entity.WareSkuEntity;
import com.alex.mallware.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wareSkuEntityQueryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wareSkuEntityQueryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));

        if(wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setStock(skuNum);
            entity.setWareId(wareId);

            //异常出现后不回滚，可以用try-catch
            //TODO: 其他方式不回滚事务
            try {
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0){
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    entity.setSkuName((String) data.get("skuName"));
                }
            }
            catch (Exception e){}



            this.baseMapper.insert(entity);
        }

        else
            this.baseMapper.addStock(skuId, wareId, skuNum);
    }

}
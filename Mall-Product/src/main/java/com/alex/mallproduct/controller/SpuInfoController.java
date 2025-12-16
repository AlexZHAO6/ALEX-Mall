package com.alex.mallproduct.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallproduct.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallproduct.entity.SpuInfoEntity;
import com.alex.mallproduct.service.SpuInfoService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * spu信息
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 *
 * SKU表示一种属性确定的单品，用于库存管理和销售统计。
 * SPU表示一种抽象的商品集合，用于商品分类和展示。
 * SKU与SPU之间存在着包含和细分的关系，一个SPU下面可以有多个SKU，而一个SKU只能属于一个SPU
 */
@RestController
@RequestMapping("mallproduct/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @PostMapping("/{spuId}/up")
    //@RequiresPermissions("mallproduct:spuinfo:list")
    public R spuUp(@PathVariable Long spuId){
        spuInfoService.up(spuId);

        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallproduct:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mallproduct:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallproduct:spuinfo:save")
    public R save(@RequestBody SpuSaveVo spuInfo){
		//spuInfoService.save(spuInfo);
        spuInfoService.saveSpuInfo(spuInfo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallproduct:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallproduct:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }
    @GetMapping("/skuId/{id}")
    public R getSpuInfoBuSkuId(@PathVariable("id") Long skuId){
        SpuInfoEntity res = spuInfoService.getSpuInfoBuSkuId(skuId);
        return R.ok().setData(res);
    }

}

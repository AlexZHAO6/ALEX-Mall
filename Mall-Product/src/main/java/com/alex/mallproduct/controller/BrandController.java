package com.alex.mallproduct.controller;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.common.valid.AddGroup;
import com.alex.common.valid.UpdateGroup;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.alex.mallproduct.entity.BrandEntity;
import com.alex.mallproduct.service.BrandService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;




/**
 * 品牌
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
@RestController
@RequestMapping("mallproduct/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallproduct:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brandEntityList = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brands", brandEntityList);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("mallproduct:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallproduct:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallproduct:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		//brandService.updateById(brand);
        brandService.updateDetails(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallproduct:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}

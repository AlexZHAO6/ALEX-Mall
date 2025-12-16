package com.alex.mallware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallware.vo.LockStockResultVO;
import com.alex.mallware.vo.SkuHasStockVO;
import com.alex.mallware.vo.WareLockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallware.entity.WareSkuEntity;
import com.alex.mallware.service.WareSkuService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * 商品库存
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:29:18
 */
@RestController
@RequestMapping("mallware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallware:waresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mallware:waresku:info")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallware:waresku:save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallware:waresku:update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallware:waresku:delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /*
    * Search if the sku has stock
    *
    * */
    @PostMapping("/hasstock")
    //@RequiresPermissions("mallware:waresku:list")
    public R getSkuHasStock(@RequestBody List<Long> skuIds){
        List<SkuHasStockVO> res = wareSkuService.getSkuHasStock(skuIds);

        return R.ok().setData(res);
    }

    @PostMapping("/lock/order")
    public R orderLockStock(@RequestBody WareLockVO vo){
        List<LockStockResultVO> res = wareSkuService.orderLockStock(vo);
        return R.ok().setData(res);
    }

}

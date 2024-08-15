package com.alex.mallware.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallware.vo.MergeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallware.entity.PurchaseEntity;
import com.alex.mallware.service.PurchaseService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * 采购信息
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-09 11:29:18
 */
@RestController
@RequestMapping("mallware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    @PostMapping("/merge")
    //@RequiresPermissions("mallware:purchase:list")
    public R merge(@RequestBody MergeVO mergeVO){
        purchaseService.mergePurchase(mergeVO);

        return R.ok();
    }

    @RequestMapping("/unreceived/list")
    //@RequiresPermissions("mallware:purchase:list")
    public R unreceivedList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivedPurchase(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("mallware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

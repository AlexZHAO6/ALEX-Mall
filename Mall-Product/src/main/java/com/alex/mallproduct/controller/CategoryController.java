package com.alex.mallproduct.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallproduct.entity.CategoryEntity;
import com.alex.mallproduct.service.CategoryService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * 商品三级分类
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
@RestController
@RequestMapping("mallproduct/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("mallproduct:category:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("mallproduct:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallproduct:category:save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallproduct:category:update")
    public R update(@RequestBody CategoryEntity category){
		//categoryService.updateById(category);
        categoryService.updateCascade(category);
        return R.ok();
    }

    //batch update
    @RequestMapping("/update/list")
    //@RequiresPermissions("mallproduct:category:update")
    public R updateList(@RequestBody CategoryEntity[] categoryList){
        categoryService.updateBatchById(Arrays.asList(categoryList));

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallproduct:category:delete")
    public R delete(@RequestBody Long[] catIds){
        categoryService.removeMenuByIds(Arrays.asList(catIds));
		//categoryService.removeByIds(Arrays.asList(catIds));

        return R.ok();
    }


    @RequestMapping("/list/tree")
    public R listWithTree(){
        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("tree: ", entities);
    }
}

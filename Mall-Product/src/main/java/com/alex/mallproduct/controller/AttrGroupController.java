package com.alex.mallproduct.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.alex.mallproduct.entity.AttrEntity;
import com.alex.mallproduct.service.AttrAttrgroupRelationService;
import com.alex.mallproduct.service.AttrService;
import com.alex.mallproduct.service.CategoryService;
import com.alex.mallproduct.vo.AttrGroupRelationVo;
import com.alex.mallproduct.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alex.mallproduct.entity.AttrGroupEntity;
import com.alex.mallproduct.service.AttrGroupService;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.R;



/**
 * 属性分组
 *
 * @author Alex@@
 * @email alex@gmail.com
 * @date 2024-07-08 10:32:36
 */
@RestController
@RequestMapping("mallproduct/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){

        List<AttrEntity> entityList = attrService.getRelationAttr(attrgroupId);

        return R.ok().put("data", entityList);
    }

    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){

        List<AttrGroupWithAttrsVo> entityList = attrGroupService.getAttrGroupWithAttrsByCatId(catelogId);

        return R.ok().put("data", entityList);
    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        relationService.saveBatch(vos);
        return R.ok();
    }


    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId){

        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("mallproduct:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable Long catelogId){
       // PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("mallproduct:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long[] path = categoryService.findCatelogPath(attrGroup.getCatelogId());

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("mallproduct:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("mallproduct:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("mallproduct:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}

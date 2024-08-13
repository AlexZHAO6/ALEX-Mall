package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.entity.AttrEntity;
import com.alex.mallproduct.service.AttrService;
import com.alex.mallproduct.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallproduct.dao.AttrGroupDao;
import com.alex.mallproduct.entity.AttrGroupEntity;
import com.alex.mallproduct.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> attrGroupEntityQueryWrapper = new QueryWrapper<AttrGroupEntity>();

        if(catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    attrGroupEntityQueryWrapper);

            return new PageUtils(page);
        }
        else {
            //基于key的模糊匹配
            attrGroupEntityQueryWrapper.eq("catelog_id", catelogId);
            if(!key.isEmpty()){
                attrGroupEntityQueryWrapper.and((obj) -> {
                    obj.like("attr_group_id", key).or().like("attr_group_name", key);
                });
            }

            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    attrGroupEntityQueryWrapper);

            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatId(Long catelogId) {
        //1. find all attrGroupIds
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));

        //2. find all attrs
        List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> relationAttrs = attrService.getRelationAttr(attrGroupWithAttrsVo.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(relationAttrs);

            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());


        return collect;
    }

}
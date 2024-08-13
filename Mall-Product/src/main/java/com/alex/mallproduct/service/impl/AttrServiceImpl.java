package com.alex.mallproduct.service.impl;

import com.alex.common.constant.ProductConstant;
import com.alex.mallproduct.dao.AttrAttrgroupRelationDao;
import com.alex.mallproduct.dao.AttrGroupDao;
import com.alex.mallproduct.entity.AttrAttrgroupRelationEntity;
import com.alex.mallproduct.entity.AttrGroupEntity;
import com.alex.mallproduct.service.CategoryService;
import com.alex.mallproduct.vo.AttrGroupWithAttrsVo;
import com.alex.mallproduct.vo.AttrRespVo;
import com.alex.mallproduct.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallproduct.dao.AttrDao;
import com.alex.mallproduct.entity.AttrEntity;
import com.alex.mallproduct.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttrVo(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        //do not need to set by fields manually, use this!

        //1. save basic info
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        //2. save attrGroup info
        AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
        if(attr.getAttrGroupId() != null){
            entity.setAttrGroupId(attr.getAttrGroupId());
            entity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(entity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().
                eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE : ProductConstant.AttrEnum.ATTR_TYPE_SALE);
        if(catelogId != 0){
            attrEntityQueryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if(!key.isEmpty()){
            attrEntityQueryWrapper.and((wrapper) -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                attrEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        if(attrEntity == null) return null;
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        //1. set up group info
        AttrAttrgroupRelationEntity entity = attrAttrgroupRelationDao.
                selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if(entity != null){
            attrRespVo.setAttrGroupId(entity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(entity.getAttrGroupId());
            if(attrGroupEntity != null)
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
        }

        //2. set up catelog info
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);
        if(categoryService.getById(catelogId).getName() != null)
            attrRespVo.setCatelogName(categoryService.getById(catelogId).getName());

        return attrRespVo;
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> attrGroupId = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> attrList = attrGroupId.stream().map(e -> e.getAttrId()).toList();

        List<AttrEntity> attrEntities = null;
        if(attrList != null && attrList.size() != 0)
            attrEntities = this.listByIds(attrList);
        return attrEntities;
    }

    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //当前分组只能关联别的分组没有引用的属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        //1. get all groupIds in the catelogId
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>()
                .eq("catelog_id", catelogId));

        List<Long> groupIds = attrGroupEntities.stream().map(e -> e.getAttrGroupId()).toList();
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = null;
        List<Long> attrList = null;

        //2. get attrIds within those groupIds on attrAttrgroupRelationTable
        if(groupIds.size() != 0){
            attrAttrgroupRelationEntities = attrAttrgroupRelationDao.
                    selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
            attrList = attrAttrgroupRelationEntities.stream().map(e -> e.getAttrId()).toList();
        }

        //3. find all attrs with the catelogId and remove above attrIds that has been related by a group already
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId);
        if(attrList != null && attrList.size() != 0){
            attrEntityQueryWrapper.notIn("attr_id", attrList);
        }

        String key = (String) params.get("key");
        if(key != null && !key.isEmpty()){
            attrEntityQueryWrapper.and((w) -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrEntityQueryWrapper);
        return new PageUtils(page);
    }




}
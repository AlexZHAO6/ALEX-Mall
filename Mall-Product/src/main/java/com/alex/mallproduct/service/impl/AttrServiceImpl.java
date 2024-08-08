package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.dao.AttrAttrgroupRelationDao;
import com.alex.mallproduct.dao.AttrGroupDao;
import com.alex.mallproduct.entity.AttrAttrgroupRelationEntity;
import com.alex.mallproduct.entity.AttrGroupEntity;
import com.alex.mallproduct.service.CategoryService;
import com.alex.mallproduct.vo.AttrRespVo;
import com.alex.mallproduct.vo.AttrVo;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
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
        entity.setAttrGroupId(attr.getAttrGroupId());
        entity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationDao.insert(entity);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<>();
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

}
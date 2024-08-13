package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.dao.BrandDao;
import com.alex.mallproduct.dao.CategoryDao;
import com.alex.mallproduct.entity.BrandEntity;
import com.alex.mallproduct.entity.CategoryEntity;
import com.alex.mallproduct.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

import com.alex.mallproduct.dao.CategoryBrandRelationDao;
import com.alex.mallproduct.entity.CategoryBrandRelationEntity;
import com.alex.mallproduct.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {



    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private BrandService brandService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);

        this.update(categoryBrandRelationEntity, new UpdateWrapper<CategoryBrandRelationEntity>()
                .eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> entities = this.baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>()
                .eq("catelog_id", catId));

        List<BrandEntity> brandEntityList = entities.stream().map(item -> {
            Long brandId = item.getBrandId();
            BrandEntity entity = brandService.getById(brandId);
            return entity;
        }).collect(Collectors.toList());

        return brandEntityList;
    }

}
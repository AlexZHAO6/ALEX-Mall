package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.service.CategoryBrandRelationService;
import com.alex.mallproduct.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallproduct.dao.CategoryDao;
import com.alex.mallproduct.entity.CategoryEntity;
import com.alex.mallproduct.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if(catelogJSON == null){
            List<CategoryEntity> categoryEntities = listWithTreeFromDB();
            String json = JSON.toJSONString(categoryEntities);
            stringRedisTemplate.opsForValue().set("catelogJSON", json);
            return categoryEntities;
        }
        TypeReference<List<CategoryEntity>> typeReference = new TypeReference<>(){};
        List<CategoryEntity> result = JSON.parseObject(catelogJSON, typeReference);

        //TODO: deal with OutOfDirectMemory exception when high concurrency
        //done! the old version lettuce client doesn't release the connection properly, so upgrade the version
        //or use jedis client could solve the problem
        return result;
    }

    //get categories with Tree(using recursion)
    private List<CategoryEntity> listWithTreeFromDB() {

        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        List<CategoryEntity> listWithTree = categoryEntities.stream().
                filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(v -> {
                    v.setChildren(getChildren(v, categoryEntities));
                    return v;
                })
                .sorted((a, b) -> (a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort()))
                .toList();

        return listWithTree;
    }

    @Override
    public void removeMenuByIds(List<Long> list) {
        //TODO check if the menu is used by others
        baseMapper.deleteBatchIds(list);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        findParentPath(catelogId, paths);

        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);
    }

    private void findParentPath(Long catelogId, List<Long> paths){
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if(categoryEntity.getParentCid() != 0){
            findParentPath(categoryEntity.getParentCid(), paths);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> list = all.stream().filter(category -> category.getParentCid() == root.getCatId())
                .map(category -> {
                    category.setChildren(getChildren(category, all));
                    return category;
                })
                .sorted((a, b) -> (a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort()))
                .toList();

        return list;
    }

    /**
     * cascade update all the category data
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if(!category.getName().isEmpty()){
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }
}
package com.alex.mallproduct.service.impl;

import com.alex.mallproduct.service.CategoryBrandRelationService;
import com.alex.mallproduct.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Cacheable(value = "category", key = "#root.methodName", sync = true) // sync used to add lock for dealing with cache breakdown
    public List<CategoryEntity> listWithTree() {

        List<CategoryEntity> listWithTree = null;

        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        listWithTree = categoryEntities.stream().
                filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(v -> {
                    v.setChildren(getChildren(v, categoryEntities));
                    return v;
                })
                .sorted((a, b) -> (a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort()))
                .collect(Collectors.toList());


        return listWithTree;
    }

    public List<CategoryEntity> listWithTreeManuallyUseCache() {
        /**
         * 1. deal with cache penetration
         * 2. deal with cache breakdown
         * 3. deal with cache avalanche
         * 4. deal with cache and db consistency
         */

        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if(catelogJSON == null){
            List<CategoryEntity> categoryEntities = listWithTreeFromDB();

            return categoryEntities;
        }
        TypeReference<List<CategoryEntity>> typeReference = new TypeReference<>(){};
        List<CategoryEntity> result = JSON.parseObject(catelogJSON, typeReference);

        //mention! the old version lettuce client doesn't release the connection properly, so upgrade the version
        //or use jedis client could solve the problem
        return result;
    }

    //get categories with Tree(using recursion)
    private List<CategoryEntity> listWithTreeFromDB() {
        RLock lock = redissonClient.getLock("category-lock");
        lock.lock();

        List<CategoryEntity> listWithTree = null;
        try{
            List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

            listWithTree = categoryEntities.stream().
                    filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                    .map(v -> {
                        v.setChildren(getChildren(v, categoryEntities));
                        return v;
                    })
                    .sorted((a, b) -> (a.getSort() == null ? 0 : a.getSort()) - (b.getSort() == null ? 0 : b.getSort()))
                    .toList();

            String json = JSON.toJSONString(listWithTree);
            stringRedisTemplate.opsForValue().set("catelogJSON", json, 1, TimeUnit.DAYS);
        }
        finally {
            lock.unlock();
        }

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
    @Caching(evict = {
            @CacheEvict(value = "category", key = "'listWithTree'")
    })
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if(!category.getName().isEmpty()){
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }
}
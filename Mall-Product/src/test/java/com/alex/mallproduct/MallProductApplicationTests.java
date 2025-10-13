package com.alex.mallproduct;

import com.alex.mallproduct.entity.BrandEntity;
import com.alex.mallproduct.service.BrandService;
import com.alex.mallproduct.service.CategoryService;
import com.alex.mallproduct.service.impl.AttrGroupServiceImpl;
import com.alex.mallproduct.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class MallProductApplicationTests {

    @Autowired
    public BrandService brandService;
    @Autowired
    public CategoryService categoryService;
    @Autowired
    public StringRedisTemplate stringRedisTemplate;
    @Autowired
    public RedissonClient redissonClient;
    @Autowired
    public AttrGroupServiceImpl attrGroupService;

    @Test
    public void testJoinFunc(){
        List<SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupService.getAttrGroupWithAttrsBySpuId(1L, 1L);
        System.out.println(attrGroupWithAttrsBySpuId.toString());

    }
    @Test
    public void testFindPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("data: " + Arrays.asList(catelogPath));
    }
    @Test
    void contextLoads() {
        List<BrandEntity> b = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        b.forEach((item) -> System.out.println(item));

        System.out.println("go!");
    }
    @Test
    public void testRedis(){
        stringRedisTemplate.opsForValue().set("hello", "world ahaha");
        System.out.println(stringRedisTemplate.opsForValue().get("hello"));
    }

    @Test
    public void testRedisson(){

        System.out.println(redissonClient);
    }

}

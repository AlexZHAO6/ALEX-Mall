package com.alex.mallproduct;

import com.alex.mallproduct.entity.BrandEntity;
import com.alex.mallproduct.service.BrandService;
import com.alex.mallproduct.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class MallProductApplicationTests {

    @Autowired
    public BrandService brandService;
    @Autowired
    public CategoryService categoryService;

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

}

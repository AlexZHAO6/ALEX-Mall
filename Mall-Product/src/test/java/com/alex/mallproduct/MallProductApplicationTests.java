package com.alex.mallproduct;

import com.alex.mallproduct.entity.BrandEntity;
import com.alex.mallproduct.service.BrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    public BrandService brandService;
    @Test
    void contextLoads() {
        List<BrandEntity> b = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        b.forEach((item) -> System.out.println(item));

        System.out.println("go!");
    }

}

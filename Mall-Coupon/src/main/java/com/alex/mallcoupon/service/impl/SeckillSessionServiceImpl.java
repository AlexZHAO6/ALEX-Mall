package com.alex.mallcoupon.service.impl;

import com.alex.mallcoupon.entity.SeckillSkuRelationEntity;
import com.alex.mallcoupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallcoupon.dao.SeckillSessionDao;
import com.alex.mallcoupon.entity.SeckillSessionEntity;
import com.alex.mallcoupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    private SeckillSessionDao seckillSessionDao;

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        String start = getStartTime();
        String end = getEndTime();

        List<SeckillSessionEntity> res = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", start, end));

        if(res != null && res.size() > 0){
            List<SeckillSessionEntity> collect = res.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));

                session.setRelationEntities(relationEntities);
                return session;
            }).collect(Collectors.toList());

            return collect;
        }

        return null;
    }

    private String getStartTime(){
        LocalDate now = LocalDate.now();
        LocalDate start = now.atStartOfDay().toLocalDate();

        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
    private String getEndTime(){
        LocalDate now = LocalDate.now();
        LocalDate end = now.plusDays(2).atTime(23,59,59).toLocalDate();

        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}
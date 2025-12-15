package com.alex.mallware.service.impl;

import com.alex.common.utils.R;
import com.alex.mallware.feign.MemberFeignService;
import com.alex.mallware.vo.MemberAddressVO;
import com.alex.mallware.vo.ShippingResponseVO;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallware.dao.WareInfoDao;
import com.alex.mallware.entity.WareInfoEntity;
import com.alex.mallware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wareInfoEntityQueryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wareInfoEntityQueryWrapper
        );

        return new PageUtils(page);
    }
    //calculate the shippingFee based on addr
    @Override
    public ShippingResponseVO getShippingFee(Long addrId) {
        R info = memberFeignService.info(addrId);
        MemberAddressVO memberAddressVO = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVO>() {
        });

        ShippingResponseVO shippingResponseVO = new ShippingResponseVO();
        shippingResponseVO.setAddress(memberAddressVO);
        if(memberAddressVO!=null){
            //get the last num of phone number as shipping fee -- just for demo
            String phone = memberAddressVO.getPhone();
            char lastNum = phone.charAt(phone.length() - 1);
            shippingResponseVO.setShippingFee(BigDecimal.valueOf(Integer.parseInt(String.valueOf(lastNum)));
        }

        return shippingResponseVO;
    }

}
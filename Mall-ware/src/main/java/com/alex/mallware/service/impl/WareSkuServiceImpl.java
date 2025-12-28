package com.alex.mallware.service.impl;

import com.alex.common.to.mq.OrderTO;
import com.alex.common.to.mq.StockDetailTO;
import com.alex.common.to.mq.StockLockedTO;
import com.alex.common.utils.R;
import com.alex.mallware.entity.WareOrderTaskDetailEntity;
import com.alex.mallware.entity.WareOrderTaskEntity;
import com.alex.mallware.exception.NoStockException;
import com.alex.mallware.feign.OrderFeignService;
import com.alex.mallware.feign.ProductFeignService;
import com.alex.mallware.service.WareOrderTaskDetailService;
import com.alex.mallware.service.WareOrderTaskService;
import com.alex.mallware.vo.*;
import com.alibaba.fastjson2.TypeReference;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alex.common.utils.PageUtils;
import com.alex.common.utils.Query;

import com.alex.mallware.dao.WareSkuDao;
import com.alex.mallware.entity.WareSkuEntity;
import com.alex.mallware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@RabbitListener(queues = "stock.release.stock.queue")
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private OrderFeignService orderFeignService;


    @RabbitHandler
    @Transactional
    public void handleOrderCloseRelease(OrderTO order, Message message, Channel channel) throws IOException {
        try {
            //handle the stock release
            String orderSn = order.getOrderSn();
            //find the task entity
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
            Long id = wareOrderTaskEntity.getId();
            //find all locked stock details
            List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.list(
                    new QueryWrapper<WareOrderTaskDetailEntity>()
                            .eq("task_id", id)
                            .eq("lock_status", 1)
            );
            for (WareOrderTaskDetailEntity detailEntity : detailEntities){
                this.baseMapper.unlockStock(
                        detailEntity.getSkuId(),
                        detailEntity.getWareId(),
                        detailEntity.getSkuNum()
                );
                //update the lock status
                detailEntity.setLockStatus(2);//unlocked
                wareOrderTaskDetailService.updateById(detailEntity);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
    @RabbitHandler
    @Transactional
    public void handleStockLockedRelease(StockLockedTO to, Message message, Channel channel) throws IOException {
        //TODO handle the stock release
        Long id = to.getId();
        StockDetailTO stockDetailTO = to.getDetails();

        Long detailId = stockDetailTO.getId();
        WareOrderTaskDetailEntity res = wareOrderTaskDetailService.getById(detailId);
        if(res != null){
            WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getById(id);
            String orderSn = wareOrderTaskEntity.getOrderSn();
            //check the order status
            //TODO call order service to get order status
            R orderInfo = orderFeignService.getOrderInfo(orderSn);
            if(orderInfo.getCode() == 0){
                OrderVO orderData = orderInfo.getData(new TypeReference<OrderVO>() {
                });
                if(orderData == null || orderData.getStatus() == 4){
                    //order is cancelled or does not exist
                    //release the stock
                    this.baseMapper.unlockStock(
                            stockDetailTO.getSkuId(),
                            stockDetailTO.getWareId(),
                            stockDetailTO.getSkuNum()
                    );

                    //update the lock status
                    //1 --- locked, 2 --- unlocked
                    res.setLockStatus(2);//unlocked
                    wareOrderTaskDetailService.updateById(res);

                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
                else {
                    //order is not cancelled, do not unlock
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                }
            }
            else {
                //order service call failed, requeue the message
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        }
        else {
            //detail record does not exist, no need to unlock
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wareSkuEntityQueryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            wareSkuEntityQueryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            wareSkuEntityQueryWrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wareSkuEntityQueryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(
                new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));

        if(wareSkuEntities == null || wareSkuEntities.size() == 0){
            WareSkuEntity entity = new WareSkuEntity();
            entity.setSkuId(skuId);
            entity.setStock(skuNum);
            entity.setWareId(wareId);

            //异常出现后不回滚，可以用try-catch
            //TODO: 其他方式不回滚事务
            try {
                R info = productFeignService.info(skuId);
                if(info.getCode() == 0){
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    entity.setSkuName((String) data.get("skuName"));
                }
            }
            catch (Exception e){}

            this.baseMapper.insert(entity);
        }

        else
            this.baseMapper.addStock(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVO> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVO skuHasStockVO = new SkuHasStockVO();

            //query the stock in DB
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVO.setSkuId(skuId);
            skuHasStockVO.setHasStock(count == null ? false : count > 0);
            return skuHasStockVO;
        }).toList();

        return collect;
    }

    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareLockVO vo) {
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        List<OrderItemVO> locks = vo.getLocks();
        //find the warehouses that have stock
        List<SkuWareHasStock> wareHasStocks = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            List<Long> wareIds = baseMapper.listWareIdsHasSkuStock(skuId);
            skuWareHasStock.setWareIds(wareIds);
            skuWareHasStock.setNum(item.getCount());
            return skuWareHasStock;
        }).collect(Collectors.toList());

        //lock the stock
        for (SkuWareHasStock wareHasStock : wareHasStocks){
            Boolean skuStocked = false;
            Long skuId = wareHasStock.getSkuId();
            List<Long> wareIds = wareHasStock.getWareIds();
            if(wareIds == null || wareIds.size() == 0){
                //no stock
                throw new NoStockException(skuId);
            }
            for(Long wareId: wareIds){
                Long res = this.baseMapper.lockSkuStock(skuId, wareId, wareHasStock.getNum());
                if(res == 1){
                    skuStocked = true;

                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity();
                    wareOrderTaskDetailEntity.setSkuId(skuId);
                    wareOrderTaskDetailEntity.setSkuNum(wareHasStock.getNum());
                    wareOrderTaskDetailEntity.setTaskId(wareOrderTaskEntity.getId());
                    wareOrderTaskDetailEntity.setWareId(wareId);
                    wareOrderTaskDetailEntity.setLockStatus(1);//locked

                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    //TODO send to MQ, the stock is locked
                    StockLockedTO stockLockedTO = new StockLockedTO();
                    stockLockedTO.setId(wareOrderTaskEntity.getId());

                    StockDetailTO stockDetailTO = new StockDetailTO();
                    BeanUtils.copyProperties(wareOrderTaskDetailEntity, stockDetailTO);
                    stockLockedTO.setDetails(stockDetailTO);
                    rabbitTemplate.convertAndSend("stock-event-exchange",
                            "stock.locked", stockLockedTO);

                    break;
                }
                else {
                    continue;
                }
            }
            if(!skuStocked){
                throw new NoStockException(skuId);
            }
        }

        //all stock locked successfully
        return true;
    }


}
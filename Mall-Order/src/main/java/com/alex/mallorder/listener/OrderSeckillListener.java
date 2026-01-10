package com.alex.mallorder.listener;

import com.alex.common.to.SeckillOrderTO;
import com.alex.mallorder.entity.OrderEntity;
import com.alex.mallorder.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.seckill.order.queue")
@Slf4j
public class OrderSeckillListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SeckillOrderTO seckillOrder, Channel channel, Message message) throws IOException {

        try {
            log.info("Receive seckill order message: " + seckillOrder);
            OrderEntity order = orderService.createSeckillOrder(seckillOrder);
            orderService.closeOrder(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
        catch (Exception e){
            //fail to process the message
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }

    }
}

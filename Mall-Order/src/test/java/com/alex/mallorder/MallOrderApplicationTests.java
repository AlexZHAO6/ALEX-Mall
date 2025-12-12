package com.alex.mallorder;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;
    @Test
    void contextLoads() {
    }

    @Test
    public void createExchange(){
        amqpAdmin.declareExchange(new DirectExchange("hello-java-exchange"));
        System.out.println("Exchange created");
    }

}

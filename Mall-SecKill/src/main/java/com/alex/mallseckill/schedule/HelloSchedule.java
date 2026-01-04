//package com.alex.mallseckill.schedule;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@EnableScheduling
//@EnableAsync
//@Component
//@Slf4j
//public class HelloSchedule {
//    @Async
//    @Scheduled(cron = "*/5 * * * * ?")
//    public void hello() throws InterruptedException {
//        log.info("hello schedule");
//        Thread.sleep(3000);
//    }
//}

package com.xxr.lingtuthinktank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xxr.lingtuthinktank.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableRetry
public class LingtuThinkTankApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingtuThinkTankApplication.class, args);
    }

}

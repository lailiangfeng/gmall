package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.atguigu.gmall.pms.dao")
@EnableSwagger2
@EnableFeignClients
//@ComponentScan(basePackages = "com.atguigu.gmall.sms")
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}

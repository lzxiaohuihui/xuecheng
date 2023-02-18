package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients(basePackages={"com.xuecheng.content.feignclient"})
@MapperScan("com.xuecheng.content.mapper")
@ComponentScan("com.xuecheng")
public class XuechengApiApplication {

    @Value("${test_config.a}")
    String a;

    @Value("${test_config.b}")
    String b;

    @Value("${test_config.c}")
    String c;

    @Value("${test_config.d}")
    String d;

    @Bean
    public Integer getTest(){
        System.out.println("a="+a);
        System.out.println("b="+b);
        System.out.println("c="+c);
        System.out.println("d="+d);
        return 1;
    }

    public static void main(String[] args) {
        SpringApplication.run(XuechengApiApplication.class, args);
    }

}

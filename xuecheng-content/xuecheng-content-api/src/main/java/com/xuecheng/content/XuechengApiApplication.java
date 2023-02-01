package com.xuecheng.content;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableSwagger2Doc
@SpringBootApplication
@MapperScan("com.xuecheng.content.mapper")
@ComponentScan("com.xuecheng")
public class XuechengApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuechengApiApplication.class, args);
    }

}

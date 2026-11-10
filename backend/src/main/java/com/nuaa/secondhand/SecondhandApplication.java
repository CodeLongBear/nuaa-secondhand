package com.nuaa.secondhand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan("com.nuaa.secondhand.mapper")
public class SecondhandApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondhandApplication.class, args);
    }
}

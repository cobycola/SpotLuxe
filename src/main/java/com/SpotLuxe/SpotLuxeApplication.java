package com.SpotLuxe;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.SpotLuxe.mapper")
@SpringBootApplication
public class SpotLuxeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotLuxeApplication.class, args);
    }


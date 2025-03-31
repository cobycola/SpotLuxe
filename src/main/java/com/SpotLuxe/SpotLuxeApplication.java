package com.SpotLuxe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.SpotLuxe.mapper")
@SpringBootApplication
public class SpotLuxeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotLuxeApplication.class, args);
    }

}
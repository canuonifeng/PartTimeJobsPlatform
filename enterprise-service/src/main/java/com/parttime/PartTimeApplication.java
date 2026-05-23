package com.parttime;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.parttime.enterprise.mapper", "com.parttime.platform.mapper", "com.parttime.cservice.mapper"})
public class PartTimeApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartTimeApplication.class, args);
    }
}

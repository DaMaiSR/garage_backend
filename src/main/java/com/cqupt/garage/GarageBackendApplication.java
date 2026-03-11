package com.cqupt.garage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.cqupt.garage.mapper")
@EnableScheduling
public class GarageBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarageBackendApplication.class, args);
    }
}

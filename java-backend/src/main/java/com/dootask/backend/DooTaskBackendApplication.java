package com.dootask.backend;

// Temporarily disable MyBatis
// import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
// @MapperScan("com.dootask.backend.mapper")
public class DooTaskBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DooTaskBackendApplication.class, args);
    }
}
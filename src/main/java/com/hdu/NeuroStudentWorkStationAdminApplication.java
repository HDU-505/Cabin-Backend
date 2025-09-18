package com.hdu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.hdu"})
@MapperScan("com.hdu.mapper")
@EnableTransactionManagement
@EnableFeignClients
@EnableScheduling
public class NeuroStudentWorkStationAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeuroStudentWorkStationAdminApplication.class, args);
    }

}

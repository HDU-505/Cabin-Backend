package com.hdu;


import com.hdu.utils.experiment.ExperimentStatus;
import com.hdu.utils.experiment.ExperimentStatusManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
class NeuroStudentWorkStationAdminApplicationTests {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Autowired
    ExperimentStatusManager experimentStatusManager;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void contextLoads() {
        experimentStatusManager.setExperimentStatus("1", ExperimentStatus.ENDED);
    }

    private static final String URL = "jdbc:arrow-flight-sql://111.231.12.252:8902";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String TENANT = "cnosdb";

    private static final int N = 10000;


    Mono<String> test() {
        String url = "http://111.231.12.252:8902/api/v1/sql?db=neurostudent";
        String query = "INSERT INTO eeg (id, experiment_id, time, gnd, ref, afz, af3, af4, af7, af8, pz, p3, p4) values('test2','test',1715055787538,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0);";

        return webClientBuilder
                .build()
                .post()
                .uri(url)
                .headers(headers -> {
                    headers.setBasicAuth("root", "");
                    headers.add("Accept", "application/json");
                })
                .bodyValue(query)
                .retrieve()
                .bodyToMono(String.class);
    }
    @Test
    void test2(){
        Mono<String> mono = test();

        // 订阅并打印响应结果
        mono.subscribe(
                response -> {
                    System.out.println("Response: " + response);
                },
                error -> {
                    System.err.println("Error: " + error.getMessage());
                }
        );
    }
}

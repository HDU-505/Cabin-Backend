package com.hdu;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
class NeuroStudentWorkStationAdminApplicationTests {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
//    @Autowired
//    ExperimentStatusManager experimentStatusManager;
    @Autowired
    private WebClient.Builder webClientBuilder;


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

package com.hdu;

import com.hdu.config.CnosDBProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.EEG;
import com.hdu.entity.EOG;
import com.hdu.service.DataService;
import com.hdu.service.impl.ParadigmServiceImpl;
import com.hdu.utils.cnosdb.CnosdbUtil;
import com.hdu.utils.mqtt.MqttAcceptClient;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class TEST {
    @Autowired
    DataService dataService;

    @Autowired
    CnosdbUtil cnosdbUtil;


    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Test
    public void test(){
        List<String> jsonList = new ArrayList<>();
        jsonList.add("{\"header\":{\"type\":\"EEG\"},\"data\":[0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8]}");
        jsonList.add("{\"header\":{\"type\":\"GSR\"},\"data\":[1.5]}");
        jsonList.add("{\"header\":{\"type\":\"HR\"},\"data\":[75.0]}");
        jsonList.add("{\"header\":{\"type\":\"EOG\"},\"data\":{\"cnt\":\"100\",\"fpogx\":\"0.1\",\"fpogy\":\"0.2\",\"fpogs\":\"0.3\",\"fpogd\":\"0.4\",\"fpogid\":\"1\",\"fpogv\":\"0.5\",\"lpogx\":\"0.6\",\"lpogy\":\"0.7\",\"lpogv\":\"0.8\",\"rpogx\":\"0.9\",\"rpogy\":\"1.0\",\"rpogv\":\"1.1\"}}");
        for (String json : jsonList) {
            dataService.dataDistribute("/test",json);
        }
    }

//    @Test
//    public void test2(){
//        EEG eeg = new EEG("test2","test","1717055777538",1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0);
//        List<EEG> datas = new ArrayList<>();
//        datas.add(eeg);
//
//        String insertSQL = CnosDBProperties.EEG_INSERT + " values(";
//        //判断是否为空
//        if (datas != null && !datas.isEmpty()) {
//            //把数据一条条存储到数据库中
//            for (int i = 0; i < datas.size(); i++) {
//                EEG temp = datas.get(i);
//                //构建SQL语句
//                insertSQL += "'" + temp.getId() + "',";
//                insertSQL += "'" + temp.getExperiment_id() + "',";
//                insertSQL += temp.getTimeStamp() + ",";
//                insertSQL += temp.getGnd() + ",";
//                insertSQL += temp.getRef() + ",";
//                insertSQL += temp.getAfz() + ",";
//                insertSQL += temp.getAf3() + ",";
//                insertSQL += temp.getAf4() + ",";
//                insertSQL += temp.getAf7() + ",";
//                insertSQL += temp.getAf8() + ",";
//                insertSQL += temp.getPz() + ",";
//                insertSQL += temp.getP3() + ",";
//                insertSQL += temp.getP4();
//                insertSQL += ");";
//                //发起请求存储到数据库中
////                Mono<String> mono = webClientUtil.sendQuery(CnosDBProperties.url,CnosDBProperties.username,CnosDBProperties.password,insertSQL);
//                // 订阅并打印响应结果
////                mono.subscribe(
////                        response -> {
////                            ObjectMapper objectMapper = new ObjectMapper();
////                            try {
////                                ResponseData[] responseData = objectMapper.readValue(response, ResponseData[].class);
////                                if (responseData.length > 0 && responseData[0].getRows() > 0) {
////                                    System.out.println("Insert successful: " + responseData[0].getRows() + " row(s) affected.");
////                                } else {
////                                    System.out.println("Insert failed or no rows affected.");
////                                }
////                            } catch (Exception e) {
////                                System.err.println("Failed to parse response: " + e.getMessage());
////                            }
////                        },
////                        error -> {
////
////                        }
////                );
//            }
//        }
//
//        System.out.println(insertSQL);;
//    }

    @Test
    public void test3(){
        redisTemplate.opsForValue().set("test","test");
    }

//    @Test
//    public void test4(){
//        EOG eog = new EOG(
//                "example_id_test0",             // id
//                "example_experiment_id",  // experiment_id
//                "1667165200790401000",          // timeStamp
//                "200",                    // cnt
//                "0.1",                    // fpogx
//                "0.2",                    // fpogy
//                "0.3",                    // fpogs
//                "0.4",                    // fpogd
//                "1",                      // fpogid
//                "0.5",                    // fpogv
//                "0.6",                    // lpogx
//                "0.7",                    // lpogy
//                "0.8",                    // lpogv
//                "0.9",                    // rpogx
//                "1.0",                    // rpogy
//                "1.1"                     // rpogv
//        );
//        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY,eog);
////        EOGThread eogRunnable = new EOGThread(ExperimentProperties.experimentId,redisTemplate,cnosdbUtil);
////        Thread thread = new Thread(eogRunnable);
////        thread.start();
////        try {
////            Thread.sleep(50);
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        }
////        System.out.println("hello");
//    }
    @Test
    public void test7(){
        // 获取所有的实验数据
        List<Object> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY,0,-1);

        // 判断是否为空
        if (datas != null && !datas.isEmpty()) {
            // 创建 CountDownLatch，用于等待所有异步操作完成
            CountDownLatch latch = new CountDownLatch(datas.size());

            // 把数据一条条存储到数据库中
            for (Object test : datas) {
                EOG temp = (EOG) test;
                System.out.println(temp.toString());
                // 检查每个属性是否为空
                if (temp.getId() == null || temp.getExperiment_id() == null || temp.getTimeStamp() == null ||
                        temp.getCnt() == null || temp.getFpogx() == null || temp.getFpogy() == null ||
                        temp.getFpogs() == null || temp.getFpogd() == null || temp.getFpogid() == null ||
                        temp.getFpogv() == null || temp.getLpogx() == null || temp.getLpogy() == null ||
                        temp.getLpogv() == null || temp.getRpogx() == null || temp.getRpogy() == null ||
                        temp.getRpogv() == null) {
                    latch.countDown();
                    continue;
                }
                try {
                    // 使用 StringBuilder 构建 SQL 语句
                    StringBuilder insertSQL = new StringBuilder(CnosDBProperties.EOG_INSERT + " values(");
                    insertSQL.append("'").append(temp.getId()).append("',");
                    insertSQL.append("'").append(temp.getExperiment_id()).append("',");
                    insertSQL.append(temp.getTimeStamp()).append(",");
                    insertSQL.append("'").append(temp.getCnt()).append("',");
                    insertSQL.append("'").append(temp.getFpogx()).append("',");
                    insertSQL.append("'").append(temp.getFpogy()).append("',");
                    insertSQL.append("'").append(temp.getFpogs()).append("',");
                    insertSQL.append("'").append(temp.getFpogd()).append("',");
                    insertSQL.append("'").append(temp.getFpogid()).append("',");
                    insertSQL.append("'").append(temp.getFpogv()).append("',");
                    insertSQL.append("'").append(temp.getLpogx()).append("',");
                    insertSQL.append("'").append(temp.getLpogy()).append("',");
                    insertSQL.append("'").append(temp.getLpogv()).append("',");
                    insertSQL.append("'").append(temp.getRpogx()).append("',");
                    insertSQL.append("'").append(temp.getRpogy()).append("',");
                    insertSQL.append("'").append(temp.getRpogv()).append("'");
                    insertSQL.append(");");

                    // 打印 SQL 语句
                    System.out.println(insertSQL.toString());

                    // 发起请求存储到数据库中
//                    Mono<String> mono = webClientUtil.sendQuery(CnosDBProperties.url, CnosDBProperties.username, CnosDBProperties.password, insertSQL.toString());
//                    CompletableFuture<String> future = cnosdbRestClientUtil.executeSql(CnosDBProperties.url,CnosDBProperties.username,CnosDBProperties.password,CnosDBProperties.database,insertSQL.toString());
//                    System.out.println(future);
//                    // 订阅并打印响应结果
//                    mono.subscribe(
//                            response -> {
//                                ObjectMapper objectMapper = new ObjectMapper();
//                                try {
//                                    ResponseData[] responseData = objectMapper.readValue(response, ResponseData[].class);
//                                    if (responseData.length > 0 && responseData[0].getRows() > 0) {
//                                        System.out.println("Insert successful: " + responseData[0].getRows() + " row(s) affected.");
//                                    } else {
//                                        System.out.println("Insert failed or no rows affected.");
//                                    }
//                                } catch (Exception e) {
//                                } finally {
//                                    latch.countDown();
//                                }
//                            },
//                            error -> {
//                                latch.countDown();
//                            }
//                    );
                } catch (Exception e) {
                    latch.countDown();
                }
            }

            // 等待所有异步操作完成
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
        }
    }

    @Test
    public void test5(){
        // 获取所有的实验数据
        List<Object> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY,0,-1);

        // 判断是否为空
        if (datas != null && !datas.isEmpty()) {
            // 创建 CountDownLatch，用于等待所有异步操作完成
            CountDownLatch latch = new CountDownLatch(datas.size());

            // 把数据一条条存储到数据库中
            for (Object test : datas) {
                System.out.println(test.toString());
                EOG temp = (EOG) test;
                try {
                    // 使用 StringBuilder 构建 SQL 语句
                    StringBuilder insertSQL = new StringBuilder(CnosDBProperties.EOG_INSERT + " values(");
                    insertSQL.append("'").append(temp.getId()).append("',");
                    insertSQL.append("'").append(temp.getExperiment_id()).append("',");
                    insertSQL.append(temp.getTimeStamp()).append(",");
                    insertSQL.append("'").append(temp.getCnt()).append("',");
                    insertSQL.append("'").append(temp.getFpogx()).append("',");
                    insertSQL.append("'").append(temp.getFpogy()).append("',");
                    insertSQL.append("'").append(temp.getFpogs()).append("',");
                    insertSQL.append("'").append(temp.getFpogd()).append("',");
                    insertSQL.append("'").append(temp.getFpogid()).append("',");
                    insertSQL.append("'").append(temp.getFpogv()).append("',");
                    insertSQL.append("'").append(temp.getLpogx()).append("',");
                    insertSQL.append("'").append(temp.getLpogy()).append("',");
                    insertSQL.append("'").append(temp.getLpogv()).append("',");
                    insertSQL.append("'").append(temp.getRpogx()).append("',");
                    insertSQL.append("'").append(temp.getRpogy()).append("',");
                    insertSQL.append("'").append(temp.getRpogv()).append("'");
                    insertSQL.append(");");

                    // 打印 SQL 语句
                    System.out.println(insertSQL.toString());

                } catch (Exception e) {
                    latch.countDown();
                }
            }

            // 等待所有异步操作完成
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
        }
    }

    @Test
    public void test8() throws SQLException {
        String sql = "INSERT INTO eog (id, experiment_id, time, cnt, fpogx, fpogy, fpogs, fpogd, fpogid, fpogv, lpogx, lpogy, lpogv, rpogx, rpogy, rpogv)  values('example_id_test','example_experiment_id',11000000000010,'100','0.1','0.2','0.3','0.4','1','0.5','0.6','0.7','0.8','0.9','1.0','1.1');\n";
        cnosdbUtil.executeUpdate(sql);
    }

    @Test
    public void test9() throws SQLException {
        String eeg_select_sql = "SELECT * FROM eeg WHERE experiment_id = '27815754178709048898';";
        List<EEG> eegList = new ArrayList<>();
        //查询数据
        ResultSet eegResultSet = cnosdbUtil.executeQuery(eeg_select_sql);
        //解析数据
        try{
            while (eegResultSet.next()) {
                EEG eegData = new EEG();
                eegData.setId(eegResultSet.getString("id"));
                eegData.setExperiment_id(eegResultSet.getString("experiment_id"));
                eegData.setGnd(eegResultSet.getDouble("gnd"));
                eegData.setRef(eegResultSet.getDouble("ref"));
                eegData.setAfz(eegResultSet.getDouble("afz"));
                eegData.setAf3(eegResultSet.getDouble("af3"));
                eegData.setAf4(eegResultSet.getDouble("af4"));
                eegData.setAf7(eegResultSet.getDouble("af7"));
                eegData.setAf8(eegResultSet.getDouble("af8"));
                eegData.setPz(eegResultSet.getDouble("pz"));
                eegData.setP3(eegResultSet.getDouble("p3"));
                eegData.setP4(eegResultSet.getDouble("p4"));
                eegData.setTimeStamp(eegResultSet.getString("time"));
                eegList.add(eegData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Resource
    ParadigmServiceImpl paradigmService;
    @Test
    public void test10(){
        boolean flag = paradigmService.sendParadigmToTouchScreen("1800764439778766850");
        System.out.println(flag);
    }

    @Test
    public void test11(){
        try (org.apache.commons.compress.archivers.zip.ZipFile zipFile = new ZipFile("E:\\doing\\NeuroStudent_SignalFlow\\NeuroStudent_SignalFlow\\uploads\\22319652362230759933.zip")) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                System.out.println("Entry: " + entry.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test12(){
        cnosdbUtil.executeUpdate("INSERT INTO eog (id, experiment_id, time, cnt, fpogx, fpogy, fpogs, fpogd, fpogid, fpogv, lpogx, lpogy, lpogv, rpogx, rpogy, rpogv)  values('86352097506210912149','60714723908442642680',1720676009604,'6281','0.00000','0.00000','0.00000','0.00000','0','0','0.00000','0.00000','0','0.00000','0.00000','0');");
    }

    @Autowired
    MqttAcceptClient mqttAcceptClient;

    @Test
    public void test13(){
        mqttAcceptClient.subscribe("/test",0);
        while (true){
            continue;
        }
    }
}


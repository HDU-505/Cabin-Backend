package com.hdu.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import com.hdu.config.CnosDBCounterConfig;
import com.hdu.config.ExperimentProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.*;
import com.hdu.service.DataService;
import com.hdu.service.IEegService;
import com.hdu.thread.*;
import com.hdu.utils.IdGenerator;
import com.hdu.utils.cnosdb.CnosdbUtil;
import com.hdu.utils.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DataServiceImpl implements DataService {
    private static final Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    CnosdbUtil cnosdbUtil;
    private Thread eogThread;

    @Autowired
    CnosDBCounterConfig cnosDBCounter;

    /*
    *   数据集散函数，所有订阅的数据全都来到该处，由该处选择合适的处理
    * */
    @Override
    public void dataDistribute(String topic,String data) {
        logger.info("进入数据集散中心");
        Map<String,Object> map = getMqttMap(data);
        if (!DataType.EOG.name().equals(((String) map.get("type")).toUpperCase())){
            JSONArray jsonArray = (JSONArray) map.get("data");
            List<Double> dataList = jsonArray.toJavaList(Double.class);
            if (DataType.EEG.name().equals(((String) map.get("type")).toUpperCase()) && dataList != null && dataList.size() == 8){
                int currentCount = cnosDBCounter.incrementAndGet("eeg");
                EEG eeg = new EEG(IdGenerator.generate20CharId(),ExperimentProperties.experimentId,(String) map.get("timeStamp"),0.0,0.0, dataList.get(0),
                        dataList.get(1), dataList.get(2), dataList.get(3),dataList.get(4), dataList.get(5), dataList.get(6),dataList.get(7));
                eegDataChache(eeg);
            }else if (DataType.GSR.name().equals(((String) map.get("type")).toUpperCase()) && dataList != null && dataList.size() == 1){
                int currentCount = cnosDBCounter.incrementAndGet("gsr");
                GSR gsr = new GSR(IdGenerator.generate20CharId(),ExperimentProperties.experimentId,(String) map.get("timeStamp"),dataList.get(0),currentCount+"");
                gsrDataChache(gsr);
            }else if (DataType.HR.name().equals(((String) map.get("type")).toUpperCase()) && dataList != null && dataList.size() == 1){
                int currentCount = cnosDBCounter.incrementAndGet("hr");
                HR hr = new HR(IdGenerator.generate20CharId(),ExperimentProperties.experimentId,(String) map.get("timeStamp"),dataList.get(0),currentCount+"");
                hrDataChache(hr);
            } else if (DataType.LABEL.name().equals(((String) map.get("type")).toUpperCase()) && dataList != null && dataList.size() == 1) {
                int currentCount = cnosDBCounter.incrementAndGet("label");
                Label label = new Label(IdGenerator.generate20CharId(),ExperimentProperties.experimentId,(String) map.get("timeStamp"),dataList.get(0)+"",currentCount+"");
                labelDataChache(label);
            }
        } else {
            // 解析眼动数据
            if (map != null){
                int currentCount = cnosDBCounter.incrementAndGet("eog");
                EOG eog = new EOG(IdGenerator.generate20CharId(),ExperimentProperties.experimentId,(String) map.get("timeStamp"),(String) map.get("cnt"), (String)map.get("fpogx"),
                        (String)map.get("fpogy"),(String)map.get("fpogs"),(String)map.get("fpogd"),(String)map.get("fpogid"),(String)map.get("fpogv"),(String)map.get("lpogx"), (String)map.get("lpogy"),
                        (String)map.get("lpogv"),(String)map.get("rpogx"),(String)map.get("rpogy"),(String)map.get("rpogv"),currentCount+"");
                eogDataChache(eog);
            }
        }
    }

    /*
    *   数据持久化函数，实验结束后，所有数据均通过该处进行数据持久化
    * */
    @Override
    public void dataStoreCnosDB() {

        //把所有数据按照分类多线程同步存储到数据库
        // 获取EEG-Redis列表的长度
        Long listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY);
        if (listSize != null && listSize != 0){
            // 把数据库存储到MYsql数据库中
            // 定义每个分片的大小
            int chunkSize = 1000; // 可根据需要调整

            // 计算需要的线程数量
            int threadCount = (int) Math.ceil((double) listSize / chunkSize);
            // 创建线程池
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            // 创建和启动线程
            for (int i = 0; i < threadCount; i++) {
                int start = i * chunkSize;
                int end = Math.min((i + 1) * chunkSize - 1, listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                executorService.submit(new EEGThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no eeg data store");
        }


        // 获取GSR-Redis列表的长度
        Long gsr_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_GSR_KEY);
        if (gsr_listSize != null && gsr_listSize != 0){
            // 定义每个分片的大小
            int gsr_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int gsr_threadCount = (int) Math.ceil((double) gsr_listSize / gsr_chunkSize);
            // 创建线程池
            ExecutorService gsr_executorService = Executors.newFixedThreadPool(gsr_threadCount);
            // 创建和启动线程
            for (int i = 0; i < gsr_threadCount; i++) {
                int start = i * gsr_chunkSize;
                int end = Math.min((i + 1) * gsr_chunkSize - 1, gsr_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                gsr_executorService.submit(new GSRThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            gsr_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no gsr data store");
        }


        // 获取HR-Redis列表的长度
        Long hr_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_HR_KEY);

        if (hr_listSize != null && hr_listSize != 0){
            // 定义每个分片的大小
            int hr_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int hr_threadCount = (int) Math.ceil((double) hr_listSize / hr_chunkSize);
            // 创建线程池
            ExecutorService hr_executorService = Executors.newFixedThreadPool(hr_threadCount);
            // 创建和启动线程
            for (int i = 0; i < hr_threadCount; i++) {
                int start = i * hr_chunkSize;
                int end = Math.min((i + 1) * hr_chunkSize - 1, hr_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                hr_executorService.submit(new HRThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            hr_executorService.shutdown();

        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no hr data store");
        }

        // 获取EOG-Redis列表的长度
        Long eog_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY);

        if (eog_listSize != null && eog_listSize != 0){
            // 定义每个分片的大小
            int eog_chunkSize = 1000; // 可根据需要调整

            // 计算需要的线程数量
            int eog_threadCount = (int) Math.ceil((double) eog_listSize / eog_chunkSize);
            // 创建线程池
            ExecutorService eog_executorService = Executors.newFixedThreadPool(eog_threadCount);
            // 创建和启动线程
            for (int i = 0; i < eog_threadCount; i++) {
                int start = i * eog_chunkSize;
                int end = Math.min((i + 1) * eog_chunkSize - 1, eog_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                eog_executorService.submit(new EOGThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            eog_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no eog data store");
        }

        // 获取EOG-Redis列表的长度
        Long label_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_LABEL_KEY);

        if (label_listSize != null && label_listSize != 0){
            // 定义每个分片的大小
            int label_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int label_threadCount = (int) Math.ceil((double) label_listSize / label_chunkSize);
            // 创建线程池
            ExecutorService label_executorService = Executors.newFixedThreadPool(label_threadCount);
            // 创建和启动线程
            for (int i = 0; i < label_threadCount; i++) {
                int start = i * label_chunkSize;
                int end = Math.min((i + 1) * label_chunkSize - 1, label_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                label_executorService.submit(new LABELThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            label_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no label data store");
        }


    }

    @Autowired
    IEegService eegService;

    /*
    *   数据持久化到MYsql数据库中
    * */
    @Override
    public void dataStoreMySQL() {

        // 链接数据库
        cnosdbUtil.init();
        //把所有数据按照分类多线程同步存储到数据库
        // 获取EEG-Redis列表的长度
        Long listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY);
        if (listSize != null && listSize != 0){
            List<EEG> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY,0,listSize);

            eegService.saveBatch(datas);

            redisTemplate.opsForValue().set(RedisKeyConfig.EXPERIMENT_ON_OFF_STATUS,"off");
//            // 定义每个分片的大小
//            int chunkSize = 1000; // 可根据需要调整
//
//            // 计算需要的线程数量
//            int threadCount = (int) Math.ceil((double) listSize / chunkSize);
//            // 创建线程池
//            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
//            // 创建和启动线程
//            for (int i = 0; i < threadCount; i++) {
//                int start = i * chunkSize;
//                int end = Math.min((i + 1) * chunkSize - 1, listSize.intValue() - 1);
//                //开辟新线程持久化EEG数据
//                executorService.submit(new EEGThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
//            }
//            // 关闭线程池
//            executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no eeg data store");
        }


        // 获取GSR-Redis列表的长度
        Long gsr_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_GSR_KEY);
        if (gsr_listSize != null && gsr_listSize != 0){
            // 定义每个分片的大小
            int gsr_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int gsr_threadCount = (int) Math.ceil((double) gsr_listSize / gsr_chunkSize);
            // 创建线程池
            ExecutorService gsr_executorService = Executors.newFixedThreadPool(gsr_threadCount);
            // 创建和启动线程
            for (int i = 0; i < gsr_threadCount; i++) {
                int start = i * gsr_chunkSize;
                int end = Math.min((i + 1) * gsr_chunkSize - 1, gsr_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                gsr_executorService.submit(new GSRThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            gsr_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no gsr data store");
        }


        // 获取HR-Redis列表的长度
        Long hr_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_HR_KEY);

        if (hr_listSize != null && hr_listSize != 0){
            // 定义每个分片的大小
            int hr_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int hr_threadCount = (int) Math.ceil((double) hr_listSize / hr_chunkSize);
            // 创建线程池
            ExecutorService hr_executorService = Executors.newFixedThreadPool(hr_threadCount);
            // 创建和启动线程
            for (int i = 0; i < hr_threadCount; i++) {
                int start = i * hr_chunkSize;
                int end = Math.min((i + 1) * hr_chunkSize - 1, hr_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                hr_executorService.submit(new HRThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            hr_executorService.shutdown();

        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no hr data store");
        }

        // 获取EOG-Redis列表的长度
        Long eog_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY);

        if (eog_listSize != null && eog_listSize != 0){
            // 定义每个分片的大小
            int eog_chunkSize = 1000; // 可根据需要调整

            // 计算需要的线程数量
            int eog_threadCount = (int) Math.ceil((double) eog_listSize / eog_chunkSize);
            // 创建线程池
            ExecutorService eog_executorService = Executors.newFixedThreadPool(eog_threadCount);
            // 创建和启动线程
            for (int i = 0; i < eog_threadCount; i++) {
                int start = i * eog_chunkSize;
                int end = Math.min((i + 1) * eog_chunkSize - 1, eog_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                eog_executorService.submit(new EOGThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            eog_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no eog data store");
        }

        // 获取EOG-Redis列表的长度
        Long label_listSize = redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_LABEL_KEY);

        if (label_listSize != null && label_listSize != 0){
            // 定义每个分片的大小
            int label_chunkSize = 2000; // 可根据需要调整

            // 计算需要的线程数量
            int label_threadCount = (int) Math.ceil((double) label_listSize / label_chunkSize);
            // 创建线程池
            ExecutorService label_executorService = Executors.newFixedThreadPool(label_threadCount);
            // 创建和启动线程
            for (int i = 0; i < label_threadCount; i++) {
                int start = i * label_chunkSize;
                int end = Math.min((i + 1) * label_chunkSize - 1, label_listSize.intValue() - 1);
                //开辟新线程持久化EEG数据
                label_executorService.submit(new LABELThread(ExperimentProperties.experimentId, redisTemplate, cnosdbUtil, start, end));
            }
            // 关闭线程池
            label_executorService.shutdown();
        }else{
            logger.info("实验："+ExperimentProperties.experimentId+":no label data store");
        }

    }

    /*
    *   脑电数据缓存
    * */
    @Override
    public void eegDataChache(EEG eeg) {
        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY,eeg);
        // 将数据追加写入文件
        try (FileWriter writer = new FileWriter("eeg_data_comparison.txt", true)) { // "true" 表示追加模式
            writer.write(eeg.toString() + System.lineSeparator()); // 将 EEG 数据转换为字符串并写入文件
        } catch (IOException e) {
            e.printStackTrace(); // 处理文件写入异常
        }
    }

    /*
    *   皮肤电数据缓存
    * */
    public void gsrDataChache(GSR gsr) {
        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_GSR_KEY,gsr);
    }

    /*
     *   心率数据缓存
     * */
    @Override
    public void hrDataChache(HR hr) {
        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_HR_KEY,hr);
    }

    /*
     *   眼动数据缓存
     * */
    @Override
    public void eogDataChache(EOG eog) {
        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY,eog);

    }

    /*
     *   标签数据缓存
     * */
    @Override
    public void labelDataChache(Label label) {
        redisTemplate.opsForList().rightPush(RedisKeyConfig.EXPERIMENT_DATA_LABEL_KEY,label);
    }



    //解析json字符串为Map
    private Map<String,Object> getMqttMap(String json){
        Map<String, Object> map = JSON.parseObject(json, new TypeReference<Map<String, Object>>() {});
        return map;
    }

}

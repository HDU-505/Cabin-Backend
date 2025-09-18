package com.hdu.thread;


import com.hdu.config.CnosDBProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.EEG;
import com.hdu.service.IEegService;
import com.hdu.utils.cnosdb.CnosdbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/*
*   脑电数据持久化线程
* */
public class EEGSQLThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(EEGSQLThread.class);

    @Override
    public void run() {
        logger.info("EEG thread started");
        eegDataStore();
    }

    private String experiment_id;   //实验id
    private RedisTemplate redisTemplate;    //Redis操作类
    private CnosdbUtil cnosdbUtil;
    private int start;
    private int end;
    private IEegService eegService;

    public EEGSQLThread(String experiment_id, RedisTemplate redisTemplate, CnosdbUtil cnosdbUtil, int start, int end, IEegService eegService) {
        this.experiment_id = experiment_id;
        this.redisTemplate = redisTemplate;
        this.cnosdbUtil = cnosdbUtil;
        this.start = start;
        this.end = end;
        this.eegService = eegService;

    }

    private void eegDataStore(){
        logger.info("EEG store started");
        // 获取所有的实验数据
        List<EEG> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY,start,end);


        // 判断是否为空
        if (datas != null && !datas.isEmpty()) {

            try{
                // 把数据一条条存储到数据库中
                for (EEG temp : datas) {
                    System.out.println(temp.toString());
                    try {
                        // 使用 StringBuilder 构建 SQL 语句
                        StringBuilder insertSQL = new StringBuilder(CnosDBProperties.EEG_INSERT + " values(");
                        //构建SQL语句
                        insertSQL.append("'").append(temp.getId()).append("',");
                        insertSQL.append("'").append(temp.getExperiment_id()).append("',");
                        insertSQL.append(temp.getTimeStamp()).append(",");
                        insertSQL.append(temp.getGnd()).append(",");
                        insertSQL.append(temp.getRef()).append(",");
                        insertSQL.append(temp.getAfz()).append(",");
                        insertSQL.append(temp.getAf3()).append(",");
                        insertSQL.append(temp.getAf4()).append(",");
                        insertSQL.append(temp.getAf7()).append(",");
                        insertSQL.append(temp.getAf8()).append(",");
                        insertSQL.append(temp.getPz()).append(",");
                        insertSQL.append(temp.getP3()).append(",");
                        insertSQL.append(temp.getP4());
                        insertSQL.append(");");

                        // 打印 SQL 语句
                        System.out.println(insertSQL.toString());
                        // 重试机制
                        long startTime = System.currentTimeMillis();
                        while (true) {
                            try {
                                cnosdbUtil.executeUpdate(insertSQL.toString());
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("数据插入失败: " + e.getMessage());
                                // 等待一秒后重试
                                Thread.sleep(1000);
                                // 检查是否超过一分钟
                                if (System.currentTimeMillis() - startTime > 60000) {
                                    logger.error("超过一分钟仍无法插入数据");
                                    throw new RuntimeException("超过一分钟仍无法插入数据", e);
                                }
                            }
                        }

                    } catch (Exception e) {
                        logger.error("构建 SQL 语句时出现错误, 错误信息：" + e.getMessage());
                    }
                }
            }finally {
//                cnosdbUtil.closeResources();
            }
        } else {
            logger.info("No EOG data to store.");
        }

    }
}

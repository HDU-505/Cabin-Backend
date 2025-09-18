package com.hdu.thread;

import com.hdu.config.CnosDBProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.Label;
import com.hdu.utils.cnosdb.CnosdbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/*
*   皮肤电数据持久化线程类
* */
public class LABELThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(LABELThread.class);

    @Override
    public void run() {
        logger.info("label thread started");
        gsrDataStore();
    }

    private String experiment_id;   //实验id
    private RedisTemplate redisTemplate;    //Redis操作类
    private CnosdbUtil cnosdbUtil;
    private int start;
    private int end;

    public LABELThread(String experiment_id, RedisTemplate redisTemplate, CnosdbUtil cnosdbUtil, int start, int end) {
        this.experiment_id = experiment_id;
        this.redisTemplate = redisTemplate;
        this.cnosdbUtil = cnosdbUtil;
        this.start = start;
        this.end = end;
    }

    private void gsrDataStore(){
        logger.info("label store started");
        // 获取所有的实验数据
        List<Label> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_LABEL_KEY,start,end);

        // 判断是否为空
        if (datas != null && !datas.isEmpty()) {

            try{
                // 把数据一条条存储到数据库中
                for (Label temp : datas) {
                    System.out.println(temp.toString());
                    try {
                        // 使用 StringBuilder 构建 SQL 语句
                        StringBuilder insertSQL = new StringBuilder(CnosDBProperties.LABEL_INSERT + " values(");
                        // 构建SQL语句
                        insertSQL.append("'").append(temp.getId()).append("',");
                        insertSQL.append("'").append(temp.getExperiment_id()).append("',");
                        insertSQL.append(temp.getTimeStamp()).append(",");
                        insertSQL.append(temp.getLabel()).append(",");
                        insertSQL.append(temp.getNum());
                        insertSQL.append(");");

                        // 打印 SQL 语句
                        System.out.println(insertSQL);

                        cnosdbUtil.executeUpdate(insertSQL.toString());
                    } catch (Exception e) {
                        logger.error("构建 SQL 语句时出现错误, 错误信息：" + e.getMessage());
                        logger.error("cnosdb插入失败");
                    }
                }
            }finally {
//                cnosdbUtil.closeResources();
            }
        } else {
            logger.info("No Label data to store.");
        }

    }
}

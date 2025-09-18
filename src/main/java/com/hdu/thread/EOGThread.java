package com.hdu.thread;

import com.hdu.config.CnosDBProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.entity.EOG;
import com.hdu.utils.cnosdb.CnosdbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/*
*   眼动数据持久化线程类
* */
public class EOGThread implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(EOGThread.class);

    @Override
    public void run() {
        logger.info("EOG Thread started");
        eogDataStore();
    }

    private String experiment_id;   //实验id
    private RedisTemplate redisTemplate;    //Redis操作类
    private CnosdbUtil cnosdbUtil;  //cnosdb数据库处理工具类
    private int start;
    private int end;

    public EOGThread(String experiment_id, RedisTemplate redisTemplate, CnosdbUtil cnosdbUtil,int start, int end) {
        this.experiment_id = experiment_id;
        this.redisTemplate = redisTemplate;
        this.cnosdbUtil = cnosdbUtil;
        this.start = start;
        this.end = end;
    }

    private void eogDataStore(){
        logger.info("EOG store started");
        // 获取所有的实验数据
        List<EOG> datas = redisTemplate.opsForList().range(RedisKeyConfig.EXPERIMENT_DATA_EOG_KEY,0,-1);

        // 判断是否为空
        if (datas != null && !datas.isEmpty()) {
            try{
                // 把数据一条条存储到数据库中
                for (EOG temp : datas) {
                    System.out.println(temp.toString());
                    // 检查每个属性是否为空
                    if (temp.getId() == null || temp.getExperiment_id() == null || temp.getTimeStamp() == null ||
                            temp.getCnt() == null || temp.getFpogx() == null || temp.getFpogy() == null ||
                            temp.getFpogs() == null || temp.getFpogd() == null || temp.getFpogid() == null ||
                            temp.getFpogv() == null || temp.getLpogx() == null || temp.getLpogy() == null ||
                            temp.getLpogv() == null || temp.getRpogx() == null || temp.getRpogy() == null ||
                            temp.getRpogv() == null) {
                        logger.error("EOG data contains null fields: {}", temp);
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
                        insertSQL.append(temp.getNum());
                        insertSQL.append(");");

                        // 打印 SQL 语句
                        System.out.println(insertSQL.toString());
                        try{
                            cnosdbUtil.executeUpdate(insertSQL.toString());
                        }catch (Exception e){
                            e.printStackTrace();
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

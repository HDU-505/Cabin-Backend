package com.hdu.lister;

import com.hdu.config.ExperimentProperties;
import com.hdu.config.RedisKeyConfig;
import com.hdu.utils.experiment.ExperimentStatus;
import com.hdu.utils.experiment.ExperimentStatusManager;
import com.hdu.utils.websocket.ExperimentStatusWebsocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class DataLister {

    private static final Logger logger = LoggerFactory.getLogger(DataLister.class);

    @Autowired
    RedisTemplate redisTemplate;

    private boolean enabled = false;

    private int lastSize = -1;

    @Autowired
    ExperimentStatusWebsocketClient experimentStatusWebsocketClient;
    @Autowired
    ExperimentStatusManager experimentStatusManager;


    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void executeTask() throws Exception {
        if (enabled) {
            logger.info("定时任务正在执行...");
            int newSize = Math.toIntExact(redisTemplate.opsForList().size(RedisKeyConfig.EXPERIMENT_DATA_EEG_KEY));
            //判断是否增加，如果未增加就抛出异常
            if (newSize == lastSize){
                experimentStatusWebsocketClient.send(ExperimentProperties.experimentId+" "+"TERMINATED");
                experimentStatusManager.setExperimentStatus(ExperimentProperties.experimentId, ExperimentStatus.TERMINATED);
            }else {
                lastSize = newSize;
            }
            // 执行任务的逻辑
        } else {
            logger.info("定时任务已禁用。");
        }
    }

    // 启用定时任务
    public void enable() {
        this.enabled = true;
    }

    // 禁用定时任务
    public void disable() {
        this.enabled = false;
    }
}
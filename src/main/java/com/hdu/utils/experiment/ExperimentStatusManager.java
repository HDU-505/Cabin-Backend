package com.hdu.utils.experiment;

import com.hdu.config.RedisKeyConfig;
import com.hdu.utils.websocket.ExperimentStatusWebsocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ExperimentStatusManager {


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    ExperimentStatusWebsocketClient experimentStatusWebsocketClient;

    public void setExperimentStatus(String experimentId, ExperimentStatus status) {
        String key = getExperimentStatusKey(experimentId);
        redisTemplate.opsForValue().set(key, status);
        experimentStatusWebsocketClient.send(experimentId+" "+status);
        // 发布状态变化事件
        stringRedisTemplate.convertAndSend("experimentStatus", experimentId + ":" + status.name());
    }

    public ExperimentStatus getExperimentStatus(String experimentId) {
        String key = getExperimentStatusKey(experimentId);
        String statusStr = stringRedisTemplate.opsForValue().get(key);
        assert statusStr != null;
        return fromString(statusStr);
    }

    public boolean isExperimentStatus(String experimentId, ExperimentStatus status) {
        String key = getExperimentStatusKey(experimentId);
        ExperimentStatus currentStatus = (ExperimentStatus) redisTemplate.opsForValue().get(key);
        return status == currentStatus;
    }

    private String getExperimentStatusKey(String experimentId) {
        return RedisKeyConfig.EXPERIMENT_STATUS_KEY_PREFIX + experimentId;
    }

    public ExperimentStatus fromString(String status) {
        // 去除前后的引号
        if (status.startsWith("\"") && status.endsWith("\"")) {
            status = status.substring(1, status.length() - 1);
        }
        switch (status) {
            case "WAITING":
                return ExperimentStatus.WAITING;
            case "STARTED":
                return ExperimentStatus.STARTED;
            case "TERMINATED":
                return ExperimentStatus.TERMINATED;
            case "ENDED":
                return ExperimentStatus.ENDED;
            default:
                throw new IllegalArgumentException("Invalid ExperimentStatus string: " + status);
        }
    }
}
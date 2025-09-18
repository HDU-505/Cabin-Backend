package com.hdu.config;

import com.hdu.utils.mqtt.MqttAcceptClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @Description : 启动服务的时候开启监听客户端
 * @Author : DZL
 * @Date : 2023/10/26 16:35
 */
@Configuration
public class MqttConfig {

    @Autowired
    private MqttAcceptClient mqttAcceptClient;

    /**
     * 订阅mqtt
     *
     * @return
     */
    @Conditional(MqttCondition.class)
    @Bean
    public MqttAcceptClient getMqttPushClient() {
        mqttAcceptClient.connect();
        return mqttAcceptClient;
    }
}

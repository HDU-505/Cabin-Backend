package com.hdu.utils.mqtt;

import com.hdu.config.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MqttAcceptClient {
    private static final Logger logger = LoggerFactory.getLogger(MqttAcceptClient.class);

    private final MqttAcceptCallback mqttAcceptCallback;

    private int connNum = 0;

    @Autowired
    public MqttAcceptClient(@Lazy MqttAcceptCallback mqttAcceptCallback) {
        this.mqttAcceptCallback = mqttAcceptCallback;
        // 其他初始化操作
    }

    @Autowired
    private MqttProperties mqttProperties;

    public static MqttClient client;

    private static MqttClient getClient() {
        return client;
    }

    private List<String> subscribedTopics = new ArrayList<>();

    private static void setClient(MqttClient client) {
        MqttAcceptClient.client = client;
    }

    /**
     * 客户端连接
     */
    public void connect() {
        while (true) {
            try {
                client = new MqttClient(mqttProperties.getHostUrl(), mqttProperties.getClientId(),
                        new MemoryPersistence());
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(mqttProperties.getUsername());
                options.setPassword(mqttProperties.getPassword().toCharArray());
                options.setConnectionTimeout(mqttProperties.getTimeout());
                options.setKeepAliveInterval(mqttProperties.getKeepAlive());
                options.setAutomaticReconnect(false); // 启用自动重连
                options.setCleanSession(mqttProperties.getCleanSession());
                MqttAcceptClient.setClient(client);
                // 设置回调
                client.setCallback(mqttAcceptCallback);
                client.connect(options);
                logger.info("MqttAcceptClient 成功连接到服务器");
                if (connNum < 1){
                    mqttAcceptCallback.connectComplete(false,client.getServerURI());
                }else {
                    mqttAcceptCallback.connectComplete(true,client.getServerURI());
                }
                connNum++;
                break; // 连接成功，跳出循环
            } catch (MqttException e) {
                logger.error("MqttAcceptClient 连接失败，正在重试，错误信息: {}", e.getMessage());
                try {
                    // 连接失败后等待5秒后重试，延长重连时间间隔
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试连接时线程被中断", ex);
                }
            }
        }
    }

    /**
     * 重新连接
     */
    public void reconnection() {
        try {
            logger.info("正在尝试重连");
            // 延长重连时间间隔
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        connect();
    }

    /**
     * 订阅某个主题
     *
     * @param topic 主题
     * @param qos   连接方式
     */
    public void subscribe(String topic, int qos) {
        logger.info("========================【开始订阅主题:" + topic + "】========================");
        try {
            client.subscribe(topic, qos);
            subscribedTopics.add(topic); // 记录订阅的主题
        } catch (MqttException e) {
            logger.error("MqttAcceptClient subscribe error,message:{}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void resubscribe() {
        for (String topic : subscribedTopics) {
            subscribe(topic, 0); // 恢复订阅的主题
        }
    }

    /**
     * 取消订阅某个主题
     *
     * @param topic
     */
    public void unsubscribe(String topic) {
        logger.info("========================【取消订阅主题:" + topic + "】========================");
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            logger.error("MqttAcceptClient unsubscribe error,message:{}", e.getMessage());
            e.printStackTrace();
        }
    }

}

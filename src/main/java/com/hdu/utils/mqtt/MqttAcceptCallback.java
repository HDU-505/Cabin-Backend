package com.hdu.utils.mqtt;

import com.hdu.config.MqttProperties;
import com.hdu.service.DataService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttAcceptCallback implements MqttCallbackExtended {

    private static final Logger logger = LoggerFactory.getLogger(MqttAcceptCallback.class);

    @Autowired
    private MqttAcceptClient mqttAcceptClient;
    @Autowired
    private DataService dataService;
    @Autowired
    private MqttProperties mqttProperties;

    /**
     * 当客户端断开连接时触发
     *
     * @param cause 断开连接的原因
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.error("MQTT连接丢失，原因: {}", cause.getMessage());
        // 连接丢失后自动重连已经启用，无需手动重连
        mqttAcceptClient.reconnection();
    }

    /**
     * 当消息传递完成时触发
     *
     * @param token 消息传递令牌
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("消息传递完成, token: {}", token.getMessageId());
    }

    /**
     * 当连接成功时触发
     *
     * @param reconnect 是否为重连
     * @param serverURI 服务器URI
     */
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            logger.info("重新连接成功: {}", serverURI);
            // 重连成功后，重新订阅之前的主题
            mqttAcceptClient.resubscribe();
        } else {
            logger.info("首次连接成功: {}", serverURI);
        }
    }

    /**
     * 当消息到达时触发
     *
     * @param topic       消息的主题
     * @param message     消息的内容
     * @throws Exception 处理消息时抛出的异常
     */
    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
        logger.info("接收到消息: {} - {}", topic, new String(message.getPayload()));
        // 处理收到的消息
        dataService.dataDistribute(topic,new String(message.getPayload())); //将收到的数据统一发往集散中心
    }
}

package com.hdu.handler;

import com.hdu.config.ExperimentProperties;
import com.hdu.lister.DataLister;
import com.hdu.service.IExperimentService;
import com.hdu.utils.mqtt.MqttAcceptClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RunningHandler {
    @Resource
    private MqttAcceptClient mqttAcceptClient;
    @Resource
    private IExperimentService experimentService;

    @Resource
    DataLister dataLister;

    // 私有构造方法，防止外部实例化
    private RunningHandler() {}

    // 处理运行状态的逻辑
    public void handleRunningState() {
        log.info("实验状态处于运行期，开始订阅MQTT");
        mqttAcceptClient.subscribe("/test",0);
        dataLister.enable();
        experimentService.startExperiment(ExperimentProperties.experimentId);
    }
}

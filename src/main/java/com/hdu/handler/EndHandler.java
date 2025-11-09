package com.hdu.handler;

import com.hdu.config.ExperimentProperties;
import com.hdu.lister.DataLister;
import com.hdu.service.DataService;
import com.hdu.service.IExperimentService;
import com.hdu.utils.mqtt.MqttAcceptClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class EndHandler {
    @Resource
    private MqttAcceptClient mqttAcceptClient;

    @Resource
    DataLister dataLister;

    @Resource
    IExperimentService experimentService;

    @Resource
    DataService dataService;

    // 私有构造方法，防止外部实例化
    private EndHandler(){}

    public void handleEndState() {
        log.info("实验状态处于结束，开始持久化数据");
        mqttAcceptClient.unsubscribe("/test");
        dataLister.disable();

        dataService.dataStoreMySQL();

        // 执行数据持久化逻辑
        experimentService.endExperiment(ExperimentProperties.experimentId);
    }
}

package com.hdu.handler;

import com.hdu.lister.DataLister;
import com.hdu.service.DataService;
import com.hdu.utils.mqtt.MqttAcceptClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CleanHandler {
    @Resource
    private MqttAcceptClient mqttAcceptClient;

    @Resource
    DataLister dataLister;

    @Resource
    private DataService dataService;

    private CleanHandler(){}    // 私有构造方法，防止外部实例化

    public void handleCleanState() {
        log.info("实验状态处于清理，开始清理数据");
        dataLister.disable();
        mqttAcceptClient.unsubscribe("/test");
        dataService.cleanDataCache();
    }
}

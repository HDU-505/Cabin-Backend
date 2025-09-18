package com.hdu.lister;

import com.hdu.handler.ResetHandler;
import com.hdu.service.DataService;
import com.hdu.utils.experiment.ExperimentStatus;
import com.hdu.utils.mqtt.MqttAcceptClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExperimentStatusChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentStatusChangeListener.class);



    @Autowired
    private DataService dataService;

    @Autowired
    private MqttAcceptClient mqttAcceptClient;

    @Autowired
    DataLister dataLister;

    public void handleMessage(String message) {
        String[] parts = message.split(":");
        String experimentId = parts[0];
        String status = parts[1];
        if (ExperimentStatus.WAITING.name().equals(status)){
            logger.info("实验："+experimentId+"  处于等待状态");
        }else if (ExperimentStatus.TERMINATED.name().equals(status)){
            logger.info("实验："+experimentId+"  处于异常终止状态");
            //进行异常处理过程
            //通知所有程序重启系统
            ResetHandler.restart(new String[]{});
        }else if (ExperimentStatus.STARTED.name().equals(status)) {
//            dataService.cacheData(experimentId);
            logger.info("实验："+experimentId+"  处于开始状态");
            //开始订阅主题
            //TODO 数据主题暂时以/test代替
            mqttAcceptClient.subscribe("/test",0);
            dataLister.enable();
        } else if (ExperimentStatus.ENDED.name().equals(status)) {
//            dataService.storeData(experimentId);
            logger.info("实验："+experimentId+"  处于结束状态");
            mqttAcceptClient.unsubscribe("/test");
            //开始持久化数据
            dataService.dataStoreMySQL();
            dataLister.disable();
        }
        // 处理其他状态的变化
    }
}
package com.hdu.lister;

import com.hdu.experiment.ExperimentState;
import com.hdu.experiment.ExperimentStateMachine;
import com.hdu.handler.ResetHandler;
import com.hdu.service.DataService;
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

    private final ExperimentStateMachine experimentStateMachine = ExperimentStateMachine.getInstance();

    // 实验状态监听器
    private final ExperimentStateMachine.StateChangeListener experimentStateLister = new ExperimentStateMachine.StateChangeListener() {

        @Override
        public void onStateChange(ExperimentState oldState, ExperimentState newState) {
            handleMessage(newState);
        }

        @Override
        public void onError(ExperimentState errorState) {
            handleMessage(errorState);
        }
    };

    public ExperimentStatusChangeListener() {
        experimentStateMachine.addLister(experimentStateLister);
    }

    public void handleMessage(ExperimentState state) {
        if (state == ExperimentState.ERROR) {
            logger.info("实验状态处于异常，重启系统");
            ResetHandler.restart(new String[]{});
        } else if (state == ExperimentState.RUNNING) {
            logger.info("实验状态处于运行期，开始订阅MQTT");
            mqttAcceptClient.subscribe("/test",0);
            dataLister.enable();
        } else if (state == ExperimentState.ENDED) {
            logger.info("实验状态处于结束，开始持久化数据");
            mqttAcceptClient.unsubscribe("/test");
            //开始持久化数据
            dataService.dataStoreMySQL();
            dataLister.disable();
        }
        // 处理其他状态的变化
    }
}
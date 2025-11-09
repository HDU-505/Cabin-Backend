package com.hdu.lister;

import com.hdu.experiment.ExperimentState;
import com.hdu.experiment.ExperimentStateMachine;
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


    public void handleMessage(ExperimentState state) {
    }
}
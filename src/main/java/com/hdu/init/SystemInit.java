package com.hdu.init;

import com.hdu.utils.mqtt.MqttAcceptClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/*
*   系统初始化时的一些操作
* */
@Component
public class SystemInit implements CommandLineRunner {
    @Autowired
    MqttAcceptClient mqttAcceptClient;

    @Override
    public void run(String... args) throws Exception {
        //订阅相关数据主题
//        subscribeData();
    }

    /*
    *   订阅相关主题
    * */
//    public void subscribeData(){
//        //订阅数据主题
//        //TODO 目前主题设定为/test
//        mqttAcceptClient.subscribe("/test",0);
//    }
}

package com.hdu.handler;

import com.hdu.config.ExperimentProperties;
import com.hdu.service.IExperimentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class PrepareHandler {
    @Resource
    private IExperimentService experimentService;

    // 私有构造方法，防止外部实例化
    private PrepareHandler() {}

    // 处理准备状态的逻辑
    public void handlePrepareState() {
        log.info("实验状态处于准备中，执行准备中逻辑");
        // 在这里添加准备状态的处理逻辑
        experimentService.createExperiment(ExperimentProperties.experiment);
    }
}

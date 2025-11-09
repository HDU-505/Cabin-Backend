package com.hdu.client;

import com.hdu.config.FeignClientInfoConfig;
import com.hdu.entity.ParadigmTouchScreen;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-signalflow-paradigminfo-8888", url = "http://127.0.0.1:8888", configuration = FeignClientInfoConfig.class)
public interface ParadigmClientInfo {

    @PostMapping(value = "/paradigm/forwardParadigmInfo",consumes = "application/json")
    boolean forwardParadigmInfo(@RequestBody ParadigmTouchScreen paradigmTouchScreen);
}

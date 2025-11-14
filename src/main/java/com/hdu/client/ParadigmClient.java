package com.hdu.client;

import com.hdu.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "service-signalflow--paradigm-8888", url = "http://192.168.1.2:8888", configuration = FeignClientConfig.class)
public interface ParadigmClient {
    @PostMapping(value = "/paradigm/forwardParadigm", consumes = "multipart/form-data")
    boolean forwardParadigm(@RequestPart("cover") MultipartFile coverFile,
                            @RequestPart("paradigm") MultipartFile paradigmFile);

}

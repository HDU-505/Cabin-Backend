package com.hdu.config;

import com.hdu.utils.websocket.ExperimentStatusWebsocketClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class WebsocketConfig {
    private final String experimentStatusServerUrl = "ws://127.0.0.1:8888/websocket/experimentStatusServer?username=" + AppIdentity.getIdentity();

    @Bean
    public ExperimentStatusWebsocketClient experimentStatusServerClient() {
        try {
            ExperimentStatusWebsocketClient webSocketClient =
                    new ExperimentStatusWebsocketClient(new URI(experimentStatusServerUrl));
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                webSocketClient.connect();
            }, 5, TimeUnit.SECONDS);
            return webSocketClient;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

}

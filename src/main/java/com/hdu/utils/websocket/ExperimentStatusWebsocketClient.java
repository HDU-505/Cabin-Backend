package com.hdu.utils.websocket;

import com.hdu.config.ExperimentProperties;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 中间件服务器用于状态控制的类
 */
@Slf4j
public class ExperimentStatusWebsocketClient extends WebSocketClient {

    private static final long INITIAL_RECONNECT_INTERVAL = 10; // 初始重连间隔，单位：秒
    private static final long MAX_RECONNECT_INTERVAL = 300;    // 最大重连间隔，单位：秒

    private final ScheduledExecutorService executor;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private volatile boolean manualClose = false;

    public ExperimentStatusWebsocketClient(URI serverUri) {
        super(serverUri);
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WebSocket-Reconnect-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("------ WebSocketClient onOpen ------");
        reconnectAttempts.set(0); // 重置重连次数
        sendInitialStatus();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("------ WebSocket onClose ------ Code: {}, Reason: {}, Remote: {}", code, reason, remote);
        if (!manualClose) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("------ WebSocket onError ------", ex);
        if (!this.isOpen()) {
            scheduleReconnect();
        }
    }

    @Override
    public void onMessage(String message) {
        log.info("-------- 接收到服务端数据： {} --------", message);
        // 处理接收到的消息
    }

    /**
     * 发送初始状态信息
     */
    private void sendInitialStatus() {
        // 如果有实验ID，则发送初始状态消息
        if (ExperimentProperties.experimentId != null && !ExperimentProperties.experimentId.trim().isEmpty()) {
            String statusStr = "DEFAULT_STATUS"; // 使用一个默认状态字符串或其他方式获取初始状态
            String message = ExperimentProperties.experimentId + " " + statusStr;
            this.send(message);
            log.info("Sent initial status: {}", message);
        } else {
            log.warn("Experiment ID is null or empty.");
        }
    }

    /**
     * 安排重连任务，使用指数退避算法
     */
    private void scheduleReconnect() {
        int attempts = reconnectAttempts.incrementAndGet();
        long delay = Math.min(INITIAL_RECONNECT_INTERVAL * (1L << (attempts - 1)), MAX_RECONNECT_INTERVAL);
        log.info("Scheduling reconnect attempt {} in {} seconds.", attempts, delay);

        executor.schedule(() -> {
            try {
                log.info("Attempting to reconnect...");
                this.reconnectBlocking();
            } catch (InterruptedException e) {
                log.error("Reconnection attempt interrupted.", e);
                Thread.currentThread().interrupt();
            }
        }, delay, TimeUnit.SECONDS);
    }

    /**
     * 关闭客户端并停止重连
     */
    public void closeConnection() {
        manualClose = true;
        cancelReconnect();
        try {
            this.closeBlocking();
        } catch (InterruptedException e) {
            log.error("Error while closing WebSocket connection.", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 取消重连任务
     */
    private void cancelReconnect() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            log.info("Reconnect scheduler shut down.");
        }
    }

    /**
     * 在应用关闭时调用以释放资源
     */
    public void shutdown() {
        closeConnection();
        cancelReconnect();
        log.info("WebSocket client shutdown completed.");
    }
}

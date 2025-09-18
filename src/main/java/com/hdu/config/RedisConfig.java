package com.hdu.config;

import com.hdu.lister.ExperimentStatusChangeListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 从配置文件中读取Redis主机信息
    @Value("${spring.redis.host}")
    private String redisHost;

    // 从配置文件中读取Redis端口信息
    @Value("${spring.redis.port}")
    private int redisPort;

    // 从配置文件中读取Redis密码信息
    @Value("${spring.redis.password}")
    private String redisPassword;

    // 配置Redis连接工厂
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 创建Redis的单机配置
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setPassword(redisPassword);
        // 返回Lettuce连接工厂
        return new LettuceConnectionFactory(config);
    }

    // 配置RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 创建RedisTemplate实例
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(connectionFactory);
        // 设置默认的序列化器为GenericJackson2JsonRedisSerializer，用于序列化键和值为JSON格式
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置键的序列化器为StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());
        // 设置值的序列化器为GenericJackson2JsonRedisSerializer
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 返回配置好的RedisTemplate实例
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("experimentStatus"));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(ExperimentStatusChangeListener listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }
}

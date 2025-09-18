package com.hdu.destory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class SystemDestory {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    // 在系统关闭前进行一些关闭操作
    @PreDestroy
    public void systemDestory(){
        // 将redis中的数据全都清空
        System.out.println("进行销毁操作");
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }
}

package com.example.itida.redisConfig;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Bean
    public RedisServer redisServer() throws IOException {
        redisServer = RedisServer.builder()
                .setting("maxheap 100mb")
                .port(6379)
                .build();
        redisServer.start();
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
